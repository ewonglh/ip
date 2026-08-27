package megia.model;

import megia.exception.TaskNotFoundException;
import megia.service.LocalizationService;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Stores tasks and resolves their one-based user-facing IDs.
 */
public class TaskStorage extends Storage<Task> implements Iterable<Task> {
    /**
     * Creates an empty task collection.
     */
    public TaskStorage() {
        super();
    }

    /**
     * Adds a task to the list.
     *
     * @param task Task to add.
     */
    public void addTask(Task task) {
        add(task);
    }

    /**
     * Marks a task as done.
     *
     * @param taskId One-based task ID.
     * @return Updated task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    public Task markTask(int taskId) throws TaskNotFoundException {
        Task task = getTaskById(taskId, "mark");
        task.markAsDone();
        return task;
    }

    /**
     * Marks a task as not done.
     *
     * @param taskId One-based task ID.
     * @return Updated task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    public Task unmarkTask(int taskId) throws TaskNotFoundException {
        Task task = getTaskById(taskId, "unmark");
        task.markAsNotDone();
        return task;
    }

    /**
     * Deletes the task using its one-based user-facing ID.
     *
     * @param taskId One-based task ID.
     * @return Deleted task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    public Task deleteTask(int taskId) throws TaskNotFoundException {
        return items.remove(getTaskIndex(taskId, "delete"));
    }

    /**
     * Returns the number of stored tasks.
     *
     * @return Number of stored tasks.
     */
    public int getTaskCount() {
        return items.size();
    }

    /**
     * Retrieves a task by its one-based user-facing ID.
     *
     * @param taskId One-based task ID.
     * @param operation Operation being attempted.
     * @return Matching task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    private Task getTaskById(int taskId, String operation) throws TaskNotFoundException {
        return items.get(getTaskIndex(taskId, operation));
    }

    /**
     * Validates and converts a one-based task ID to its zero-based list index.
     *
     * @param taskId One-based task ID.
     * @param operation Operation being attempted.
     * @return Zero-based list index.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    private int getTaskIndex(int taskId, String operation) throws TaskNotFoundException {
        if (taskId < 1 || taskId > items.size()) {
            throw new TaskNotFoundException(taskId, items.size(), operation);
        }
        return taskId - 1;
    }

    @Override
    public String toString() {
        return items.isEmpty()
                ? LocalizationService.getMessage("task_storage_empty")
                : LocalizationService.getMessage("task_storage_list") + "\n"
                        + IntStream.range(0, items.size())
                                .mapToObj(i -> (i + 1) + "." + items.get(i) + "\n")
                                .collect(Collectors.joining())
                                .strip();
    }

    @Override
    public Iterator<Task> iterator() {
        return items.iterator();
    }
}