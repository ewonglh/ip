package model;

import service.LocalizationService;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskStorage extends Storage<Task>{

    public TaskStorage() {
        super();
    }

    /**
     * Returns whether a user-facing task ID refers to an existing task.
     * User-facing IDs start at 1, while the backing list is zero-indexed.
     *
     * @param taskId the user-facing task ID
     * @return true when the ID refers to an existing task
     */
    public boolean isValidTaskId(int taskId) {
        return taskId >= 1 && taskId <= list.size();
    }

    public void markAsDone(int taskId) {
        getTaskById(taskId).markAsDone();
    }

    public void markAsNotDone(int taskId) {
        getTaskById(taskId).markAsNotDone();
    }

    public int getTaskCount() {
        return list.size();
    }

    /**
     * Finds a task by its user-facing ID and converts it to the list index.
     *
     * @param taskId the user-facing task ID
     * @return the matching task
     * @throws IllegalArgumentException if the ID does not exist
     */
    public Task getTaskById(int taskId) {
        return list.get(taskId - 1);
    }

    public void deleteTaskById(int taskId) {
        list.remove(taskId - 1);
    }

    @Override
    public String toString() {
        return list.isEmpty()
                ? LocalizationService.getMessage("task_storage_empty")
                : LocalizationService.getMessage("task_storage_list") + "\n" +
                IntStream.range(0, list.size())
                        // Map each int in int stream to item in list
                .mapToObj(i -> (i + 1) + "." + list.get(i).toString() + "\n")
                        // Collect all strings in stream to 1 string
                .collect(Collectors.joining()).strip();
    }

}