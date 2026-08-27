package megia.service;

import megia.model.*;

import java.io.*;
import java.util.Optional;

/**
 * Loads and saves task data using the application's comma-delimited storage format.
 * Task text is assumed not to contain commas until the storage format supports escaping.
 */
public final class LocalStorageService {
    String taskStoragePath;
    /**
     * Creates an instance of the stateless local storage service.
     * @param path The path for the task storage
     */
    public LocalStorageService(String path) {
        this.taskStoragePath = path;
    }

    /**
     * Loads tasks from the storage file.
     *
     * @return Loaded task storage, or an empty optional if the file cannot be read.
     */
    public Optional<TaskStorage> loadTaskData() {
        String line;
        TaskStorage taskStorage = new TaskStorage();
        TaskService taskService = new TaskService(taskStorage);

        try (BufferedReader br = new BufferedReader(new FileReader(taskStoragePath))) {
            while ((line = br.readLine()) != null) {
                String[] fields = line.strip().split(",");
                TaskType taskType = TaskType.valueOf(fields[0]);
                Task task = getTaskFromLine(fields, taskType);
                taskService.addTask(task);
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(taskStorage);
    }

    /**
     * Creates a task from fields parsed from one line of the storage file.
     *
     * @param data Parsed fields in the order required by the task type.
     * @param taskType Type of task represented by the fields.
     * @return Task reconstructed from the stored fields.
     */
    private static Task getTaskFromLine(String[] data, TaskType taskType) {
        boolean isDone = Boolean.parseBoolean(data[1]);
        Task task;
        switch (taskType) {
            case TaskType.DEADLINE -> {
                task = new Deadline(data[2], isDone, data[3]);
            }
            case TaskType.EVENT -> {
                task = new Event(data[2], isDone, data[3], data[4]);
            }
            default -> {
                task = new Todo(data[2], isDone);
            }
        }
        return task;
    }

    /**
     * Saves all tasks to the specified storage file.
     *
     * @param taskStorage Tasks to save.
     * @throws RuntimeException If the storage file cannot be written.
     */
    public void saveTaskData(TaskStorage taskStorage) {
        StringBuilder out = new StringBuilder();
        for (Task task : taskStorage) {
            out.append(task.encode()).append("\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(taskStoragePath))) {
            bw.write(out.toString().strip());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}