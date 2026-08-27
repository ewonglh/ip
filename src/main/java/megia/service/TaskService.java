package megia.service;

import megia.exception.TaskNotFoundException;
import megia.model.Task;
import megia.model.TaskStorage;

/**
 * Performs task operations without depending on the console UI.
 */
public class TaskService {
    private final TaskStorage taskStorage;

    /**
     * Creates a service backed by the supplied task storage.
     *
     * @param taskStorage Storage used by this service.
     */
    public TaskService(TaskStorage taskStorage) {
        this.taskStorage = taskStorage;
    }

    /**
     * Adds a task to the list.
     *
     * @param task Task to add.
     */
    public void addTask(Task task) {
        taskStorage.add(task);
    }

    /**
     * Marks a task as done.
     *
     * @param taskId One-based task ID.
     * @return Updated task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    public Task markTask(int taskId) throws TaskNotFoundException {
        Task task = getTask(taskId, "mark");
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
        Task task = getTask(taskId, "unmark");
        task.markAsNotDone();
        return task;
    }

    /**
     * Retrieves a task by its one-based user-facing ID.
     *
     * @param taskId One-based task ID.
     * @param operation Operation being attempted.
     * @return Matching task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    private Task getTask(int taskId, String operation) throws TaskNotFoundException {
        if (!taskStorage.isValidTaskId(taskId)) {
            throw new TaskNotFoundException(taskId, taskStorage.getTaskCount(), operation);
        }
        return taskStorage.getTaskById(taskId);
    }

    /**
     * Returns the number of stored tasks.
     *
     * @return Number of stored tasks.
     */
    public int getTaskCount() {
        return taskStorage.getTaskCount();
    }

    /**
     * Deletes the task using its one-based user-facing ID.
     *
     * @param taskId One-based task ID.
     * @return Deleted task.
     * @throws TaskNotFoundException If the task ID does not exist.
     */
    public Task deleteTask(int taskId) throws TaskNotFoundException {
        if (!taskStorage.isValidTaskId(taskId)) {
            throw new TaskNotFoundException(taskId, taskStorage.getTaskCount(), "delete");
        }
        Task deletedTask = taskStorage.getTaskById(taskId);
        taskStorage.deleteTaskById(taskId);
        return deletedTask;
    }
}
