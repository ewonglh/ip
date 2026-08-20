package service;

import exception.TaskNotFoundException;
import model.Task;
import model.TaskStorage;

/**
 * Performs task operations without depending on the console UI.
 */
public class TaskService {

    private final TaskStorage taskStorage;

    /**
     * Creates a service backed by the supplied task storage.
     *
     * @param taskStorage storage used by this service
     */
    public TaskService(TaskStorage taskStorage) {
        this.taskStorage = taskStorage;
    }

    /**
     * Adds a task to the list.
     *
     * @param task task to add
     */
    public void addTask(Task task) {
        taskStorage.add(task);
    }

    /**
     * Marks a task as done.
     *
     * @param taskId one-based task ID
     * @return the updated task
     */
    public Task markTask(int taskId) throws TaskNotFoundException {
        Task task = getTask(taskId, "mark");
        task.markAsDone();
        return task;
    }

    /**
     * Marks a task as not done.
     *
     * @param taskId one-based task ID
     * @return the updated task
     */
    public Task unmarkTask(int taskId) throws TaskNotFoundException {
        Task task = getTask(taskId, "unmark");
        task.markAsNotDone();
        return task;
    }

    /**
     * Retrieves a task by its one-based user-facing ID.
     *
     * @param taskId one-based task ID
     * @param operation operation being attempted
     * @return the matching task
     */
    private Task getTask(int taskId, String operation) throws TaskNotFoundException {
        if (!taskStorage.isValidTaskId(taskId)) {
            throw new TaskNotFoundException(taskId, taskStorage.getTaskCount(), operation);
        }
        return taskStorage.getTaskById(taskId);
    }

    /**
     * Returns the number of stored tasks.
     */
    public int getTaskCount() {
        return taskStorage.getTaskCount();
    }

    /**
     * Deletes the task using its one-based user-facing ID.
     *
     * @param taskId one-based task ID
     * @return the deleted task
     */
    public Task deleteTask(int taskId) throws TaskNotFoundException {
        if (!taskStorage.isValidTaskId(taskId)) {
            throw new TaskNotFoundException(taskId, taskStorage.getTaskCount(), "delete");
        }
        Task toDelete = taskStorage.getTaskById(taskId);
        taskStorage.deleteTaskById(taskId);
        return toDelete;
    }
}