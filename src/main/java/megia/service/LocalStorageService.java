package megia.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import megia.exception.StorageException;
import megia.model.Deadline;
import megia.model.Event;
import megia.model.Task;
import megia.model.TaskStorage;
import megia.model.TaskType;
import megia.model.Todo;

/**
 * Loads and saves tasks using the comma-delimited local storage format.
 * Descriptions are assumed not to contain commas.
 */
public final class LocalStorageService {
    private final String taskStoragePath;

    /**
     * Creates a local storage service for the specified task file.
     *
     * @param path Path to the task storage file.
     */
    public LocalStorageService(String path) {
        this.taskStoragePath = path;
    }

    /**
     * Loads tasks from storage.
     *
     * @return Loaded tasks, or an empty optional when the file does not exist.
     * @throws StorageException If a present file contains malformed data.
     */
    public Optional<TaskStorage> loadTaskData() throws StorageException {
        Path storagePath = Path.of(taskStoragePath);
        if (Files.notExists(storagePath)) {
            return Optional.empty();
        }

        TaskStorage taskStorage = new TaskStorage();
        try (BufferedReader reader = Files.newBufferedReader(storagePath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    taskStorage.addTask(getTaskFromLine(line.strip().split(",", -1)));
                } catch (RuntimeException exception) {
                    throw new StorageException(taskStoragePath, lineNumber);
                }
            }
        } catch (IOException exception) {
            throw new StorageException(taskStoragePath, 0);
        }
        return Optional.of(taskStorage);
    }

    private static Task getTaskFromLine(String[] data) {
        TaskType taskType = TaskType.valueOf(data[0]);
        int expectedFieldCount = taskType == TaskType.EVENT ? 5 : taskType == TaskType.DEADLINE ? 4 : 3;
        if (data.length != expectedFieldCount || data[2].isBlank() || !isBoolean(data[1])) {
            throw new IllegalArgumentException();
        }
        boolean isDone = Boolean.parseBoolean(data[1]);
        return switch (taskType) {
            case DEADLINE -> new Deadline(data[2], isDone, parseDateTime(data[3]));
            case EVENT -> new Event(data[2], isDone, parseDateTime(data[3]), parseDateTime(data[4]));
            case TODO -> new Todo(data[2], isDone);
        };
    }

    private static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private static boolean isBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    /**
     * Saves all tasks to the configured storage file.
     *
     * @param taskStorage Tasks to save.
     */
    public void saveTaskData(TaskStorage taskStorage) throws StorageException {
        StringBuilder output = new StringBuilder();
        for (Task task : taskStorage) {
            output.append(task.encode()).append("\n");
        }
        Path storagePath = Path.of(taskStoragePath).toAbsolutePath();
        Path temporaryPath = null;
        try {
            temporaryPath = Files.createTempFile(
                    storagePath.getParent(), storagePath.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(
                    temporaryPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                writer.write(output.toString().strip());
            }
            moveIntoPlace(temporaryPath, storagePath);
        } catch (IOException exception) {
            throw new StorageException(taskStoragePath, 0);
        } finally {
            if (temporaryPath != null) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException exception) {
                    // The completed save is still valid when temporary-file cleanup fails.
                }
            }
        }
    }

    private static void moveIntoPlace(Path temporaryPath, Path storagePath) throws IOException {
        try {
            Files.move(temporaryPath, storagePath,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
