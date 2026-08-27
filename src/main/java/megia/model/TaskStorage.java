package megia.model;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import megia.service.LocalizationService;

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
     * Returns whether a user-facing task ID refers to an existing task.
     * User-facing IDs start at 1, while the backing list is zero-indexed.
     *
     * @param taskId User-facing task ID.
     * @return True when the ID refers to an existing task.
     */
    public boolean isValidTaskId(int taskId) {
        return taskId >= 1 && taskId <= items.size();
    }

    /**
     * Marks the task with the specified user-facing ID as completed.
     *
     * @param taskId User-facing task ID.
     */
    public void markAsDone(int taskId) {
        getTaskById(taskId).markAsDone();
    }

    /**
     * Marks the task with the specified user-facing ID as incomplete.
     *
     * @param taskId User-facing task ID.
     */
    public void markAsNotDone(int taskId) {
        getTaskById(taskId).markAsNotDone();
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
     * Finds a task by its user-facing ID and converts it to the list index.
     *
     * @param taskId User-facing task ID.
     * @return Matching task.
     * @throws IndexOutOfBoundsException If the ID does not exist.
     */
    public Task getTaskById(int taskId) {
        return items.get(taskId - 1);
    }

    /**
     * Deletes the task with the specified user-facing ID.
     *
     * @param taskId User-facing task ID.
     */
    public void deleteTaskById(int taskId) {
        items.remove(taskId - 1);
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