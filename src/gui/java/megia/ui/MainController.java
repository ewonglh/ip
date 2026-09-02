package megia.ui;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import megia.exception.MegiaException;
import megia.model.CommandResult;
import megia.model.TaskEntry;
import megia.service.CommandExecutor;
import megia.service.LocalizationService;

/**
 * Coordinates the FXML chatbot transcript and command composer.
 */
public final class MainController {
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
        appendMessage(true, rawCommand, List.of());

        try {
            renderResult(commandExecutor.execute(rawCommand));
        } catch (MegiaException exception) {
            appendMessage(false, LocalizationService.getException(
                    exception.getErrorCode(), exception.getMessageArguments()), List.of());
        } catch (RuntimeException exception) {
            appendMessage(false, LocalizationService.getMessage("unexpected_error"), List.of());
        }
        commandInput.requestFocus();
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
        appendMessage(false, heading, taskList.entries());
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
        appendMessage(false, message, List.of(mutation.task()));
    }

    private void appendMessage(boolean isUser, String text, List<TaskEntry> tasks) {
        transcriptList.getItems().add(new TranscriptMessage(isUser, text, tasks));
        transcriptList.scrollTo(transcriptList.getItems().size() - 1);
    }

    private record TranscriptMessage(boolean isUser, String text, List<TaskEntry> tasks) {
        private TranscriptMessage {
            tasks = List.copyOf(tasks);
        }
    }

    private static final class TranscriptCell extends ListCell<TranscriptMessage> {
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
                content.getChildren().add(createTaskCard(task));
            }
            content.getStyleClass().add(message.isUser() ? "user-message" : "assistant-message");
            setGraphic(content);
            setText(null);
        }

        private static HBox createTaskCard(TaskEntry entry) {
            Label idLabel = new Label(Integer.toString(entry.id()));
            idLabel.getStyleClass().add("task-id");
            Label taskLabel = new Label(entry.task().toString());
            taskLabel.setWrapText(true);
            HBox card = new HBox(10, idLabel, taskLabel);
            card.getStyleClass().add("task-card");
            return card;
        }
    }
}
