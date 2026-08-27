package megia.service;

import megia.model.*;

import java.io.*;
import java.util.Optional;

public final class LocalStorageService {

    public static Optional<TaskStorage> loadTaskData(String dataPath) {
        String line;
        TaskStorage taskStorage = new TaskStorage();
        TaskService taskService = new TaskService(taskStorage);

        try (BufferedReader br = new BufferedReader(new FileReader(dataPath))) {
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

    public static void saveTaskData(TaskStorage taskStorage, String dataPath) {
        StringBuilder out = new StringBuilder();
        for (Task task : taskStorage) {
            out.append(task.encode()).append("\n");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dataPath))) {
            bw.write(out.toString().strip());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}