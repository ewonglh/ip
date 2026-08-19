package service;

import model.*;

import java.util.Objects;

public class TaskService {

    public static void handleTaskStatusCommand(String cmd, String body, TaskStorage taskStorage) {
        final int taskId;

        try {
            taskId = Integer.parseInt(body.trim());
        } catch (NumberFormatException e) {
            MessageSenderService.sendMessage(LocalizationService.getMessage("task_invalid_id"));
            return;
        }

        if (!taskStorage.isValidTaskId(taskId)) {
            MessageSenderService.sendMessage(LocalizationService.getMessage("task_invalid_id"));
            return;
        }

        String messageKey;
        if (cmd.equals("mark")) {
            taskStorage.markAsDone(taskId);
            messageKey = "task_storage_mark";
        } else {
            taskStorage.markAsNotDone(taskId);
            messageKey = "task_storage_unmark";
        }

        MessageSenderService.sendMessage(
                LocalizationService.getMessage(messageKey) + "\n" + taskStorage.getTask(taskId));
    }

    public static void parseTaskAddition(String cmd, String rawTask, TaskStorage taskStorage) {
        String[] tokens = rawTask.split(" ");
        Task newTask = new Task("");

        switch (cmd) {
            case "todo" -> {
                newTask = new ToDo(rawTask);
            }
            case "deadline" -> {
                StringBuilder task = new StringBuilder(), by = new StringBuilder();
                boolean foundBy = false;
                for (String token : tokens) {
                    if (token.equals("/by")) { foundBy = true;}

                    else if (!foundBy) { task.append(token).append(" "); }

                    else { by.append(token).append(" "); }

                    newTask = new Deadline(task.toString(), by.toString());
                }
            }
            case "event" -> {
                StringBuilder task = new StringBuilder(), from = new StringBuilder(), by = new StringBuilder();
                boolean foundTo = false;
                boolean foundFrom = false;
                for (String token : tokens) {
                    if (token.equals("/to")) { foundTo = true; foundFrom = false; }

                    else if (token.equals("/from")) { foundTo = false; foundFrom = true; }

                    else if (foundFrom) { from.append(token).append(" "); }

                    else if (foundTo) { by.append(token).append(" "); }

                    else { task.append(token).append(" "); }

                    newTask = new Event(task.toString(), from.toString(), by.toString());
                }
            }
        }

        taskStorage.add(newTask);
        MessageSenderService.sendMessage(
                LocalizationService.getMessage("task_storage_add") +
                "\n" +
                newTask +
                String.format(
                        LocalizationService.getMessage("task_storage_add_2"),
                        taskStorage.getTaskCount()));
    }
}