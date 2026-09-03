package megia.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import megia.exception.MegiaException;
import megia.model.CommandResult;
import megia.model.Deadline;
import megia.model.Event;
import megia.model.Task;
import megia.model.TaskEntry;
import megia.model.Todo;
import megia.service.CommandExecutor;
import megia.service.LocalizationService;

/**
 * Coordinates the FXML chatbot transcript and command composer.
 */
public final class MainController {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ENGLISH);

    @FXML
    private ListView<TranscriptMessage> transcriptList;
    @FXML
    private TextField commandInput;
    @FXML
    private Button sendButton;

    private final CommandExecutor commandExecutor;
    private final String startupError;

    /**
     * Creates a chatbot controller with its command executor and optional startup error.
     *
     * @param commandExecutor Shared command executor used by the chatbot.
     * @param startupError Localized startup error, or null when startup was clean.
     */
    public MainController(CommandExecutor commandExecutor, String startupError) {
        this.commandExecutor = commandExecutor;
        this.startupError = startupError;
    }

    /**
     * Configures the transcript cell factory and adds the initial greeting.
     */
    @FXML
    public void initialize() {
        transcriptList.setCellFactory(ignored -> new TranscriptCell());
        appendMessage(false, LocalizationService.getMessage("greeting"), List.of());
        if (startupError != null) {
            appendMessage(false, startupError, List.of());
        }
        commandInput.requestFocus();
    }

    /**
     * Executes the command in the composer and appends both conversation turns.
     */
    @FXML
    public void handleSend() {
        String rawCommand = commandInput.getText();
        commandInput.clear();
        submitCommand(rawCommand);
        commandInput.requestFocus();
    }

    private void submitCommand(String rawCommand) {
        appendMessage(true, rawCommand, List.of());

        try {
            renderResult(commandExecutor.execute(rawCommand));
        } catch (MegiaException exception) {
            appendMessage(false, LocalizationService.getException(
                    exception.getErrorCode(), exception.getMessageArguments()), List.of());
        } catch (RuntimeException exception) {
            appendMessage(false, LocalizationService.getMessage("unexpected_error"), List.of());
        }
    }

    /**
     * Places a todo example in the command composer.
     */
    @FXML
    public void handleStarterTodo() {
        setStarterCommand("todo ");
    }

    /**
     * Places a list example in the command composer.
     */
    @FXML
    public void handleStarterList() {
        setStarterCommand("list");
    }

    /**
     * Places a find example in the command composer.
     */
    @FXML
    public void handleStarterFind() {
        setStarterCommand("find ");
    }

    /**
     * Displays a localized system error in the assistant side of the transcript.
     *
     * @param message Error message to display.
     */
    public void showErrorMessage(String message) {
        appendMessage(false, message, List.of());
    }

    private void setStarterCommand(String command) {
        commandInput.setText(command);
        commandInput.positionCaret(command.length());
        commandInput.requestFocus();
    }

    private void renderResult(CommandResult result) {
        switch (result) {
            case CommandResult.TaskList taskList -> renderTaskList(taskList);
            case CommandResult.TaskMutation mutation -> renderMutation(mutation);
            case CommandResult.Empty ignored -> appendMessage(
                    false, LocalizationService.getMessage("empty"), List.of());
            case CommandResult.Exit ignored -> {
                appendMessage(false, LocalizationService.getMessage("farewell"), List.of());
                commandInput.setDisable(true);
                sendButton.setDisable(true);
            }
        }
    }

    private void renderTaskList(CommandResult.TaskList taskList) {
        if (taskList.entries().isEmpty()) {
            String message = switch (taskList.query().type()) {
                case ALL -> LocalizationService.getMessage("task_storage_empty");
                case DATE -> String.format(
                        LocalizationService.getMessage("task_storage_date_empty"),
                        taskList.query().value());
                case FIND -> String.format(
                        LocalizationService.getMessage("task_storage_find_empty"),
                        taskList.query().value());
            };
            appendMessage(false, message, List.of());
            return;
        }

        String heading = switch (taskList.query().type()) {
            case ALL -> LocalizationService.getMessage("task_storage_list");
            case DATE -> String.format(
                    LocalizationService.getMessage("task_storage_date_list"),
                    taskList.query().value());
            case FIND -> LocalizationService.getMessage("task_storage_find_list");
        };
        appendMessage(false, heading, taskList.entries(), true);
    }

    private void renderMutation(CommandResult.TaskMutation mutation) {
        String message;
        if (mutation.operation() == CommandResult.MutationType.ADD) {
            message = LocalizationService.getMessage("task_storage_add") + "\n"
                    + String.format(
                            LocalizationService.getMessage("task_storage_add_2"),
                            mutation.taskCount());
        } else {
            String messageKey = "task_storage_" + mutation.operation().name().toLowerCase();
            message = LocalizationService.getMessage(messageKey);
        }
        appendMessage(false, message, List.of(mutation.task()),
                mutation.operation() != CommandResult.MutationType.DELETE);
    }

    private void appendMessage(boolean isUser, String text, List<TaskEntry> tasks) {
        appendMessage(isUser, text, tasks, !isUser);
    }

    private void appendMessage(
            boolean isUser, String text, List<TaskEntry> tasks, boolean areTasksActionable) {
        transcriptList.getItems().add(new TranscriptMessage(isUser, text, tasks, areTasksActionable));
        transcriptList.scrollTo(transcriptList.getItems().size() - 1);
    }

    private void confirmAndDelete(TaskEntry entry) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(LocalizationService.getMessage("delete_confirmation_title"));
        confirmation.setHeaderText(LocalizationService.getMessage("delete_confirmation_header"));
        confirmation.setContentText(String.format(
                LocalizationService.getMessage("delete_confirmation_content"),
                entry.id(), entry.task().getDescription()));
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            submitCommand("delete " + entry.id());
        }
    }

    private HBox createTaskActions(TaskEntry entry) {
        String toggleCommand = entry.task().isDone() ? "unmark " : "mark ";
        String toggleLabel = entry.task().isDone()
                ? LocalizationService.getMessage("task_action_unmark")
                : LocalizationService.getMessage("task_action_mark");
        Button toggleButton = new Button(toggleLabel);
        toggleButton.setOnAction(ignored -> submitCommand(toggleCommand + entry.id()));

        Button deleteButton = new Button(LocalizationService.getMessage("task_action_delete"));
        deleteButton.setOnAction(ignored -> confirmAndDelete(entry));

        HBox actions = new HBox(8, toggleButton, deleteButton);
        actions.getStyleClass().add("task-actions");
        return actions;
    }

    private static String getTaskTypeKey(Task task) {
        if (task instanceof Todo) {
            return "task_type_todo";
        }
        if (task instanceof Deadline) {
            return "task_type_deadline";
        }
        if (task instanceof Event) {
            return "task_type_event";
        }
        return "task_type_task";
    }

    private static String getTaskDetails(Task task) {
        if (task instanceof Deadline deadline) {
            return String.format(
                    LocalizationService.getMessage("task_deadline_details"),
                    formatDateTime(deadline.getDeadline()));
        }
        if (task instanceof Event event) {
            return String.format(
                    LocalizationService.getMessage("task_event_details"),
                    formatDateTime(event.getStartTime()), formatDateTime(event.getEndTime()));
        }
        return "";
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMATTER);
    }

    private record TranscriptMessage(
            boolean isUser, String text, List<TaskEntry> tasks, boolean areTasksActionable) {
        private TranscriptMessage {
            tasks = List.copyOf(tasks);
        }
    }

    private final class TranscriptCell extends ListCell<TranscriptMessage> {
        @Override
        protected void updateItem(TranscriptMessage message, boolean empty) {
            super.updateItem(message, empty);
            if (empty || message == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox content = new VBox(8);
            Label messageLabel = new Label(message.text());
            messageLabel.setWrapText(true);
            content.getChildren().add(messageLabel);
            for (TaskEntry task : message.tasks()) {
                content.getChildren().add(createTaskCard(task, message.areTasksActionable()));
            }
            content.getStyleClass().add(message.isUser() ? "user-message" : "assistant-message");
            setGraphic(content);
            setText(null);
        }

        private VBox createTaskCard(TaskEntry entry, boolean areTasksActionable) {
            Label idLabel = new Label(Integer.toString(entry.id()));
            idLabel.getStyleClass().add("task-id");
            Label typeLabel = new Label(LocalizationService.getMessage(getTaskTypeKey(entry.task())));
            typeLabel.getStyleClass().add("task-type");
            HBox heading = new HBox(10, idLabel, typeLabel);

            Label descriptionLabel = new Label(entry.task().getDescription());
            descriptionLabel.getStyleClass().add("task-description");
            descriptionLabel.setWrapText(true);

            Label statusLabel = new Label(LocalizationService.getMessage(
                    entry.task().isDone() ? "task_status_done" : "task_status_pending"));
            statusLabel.getStyleClass().add("task-meta");

            VBox card = new VBox(8, heading, descriptionLabel, statusLabel);
            String details = getTaskDetails(entry.task());
            if (!details.isBlank()) {
                Label detailsLabel = new Label(details);
                detailsLabel.getStyleClass().add("task-meta");
                detailsLabel.setWrapText(true);
                card.getChildren().add(detailsLabel);
            }
            if (areTasksActionable) {
                card.getChildren().add(createTaskActions(entry));
            }
            card.getStyleClass().add("task-card");
            return card;
        }
    }
}
