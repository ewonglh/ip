package megia.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import megia.model.TaskStorage;
import megia.service.CommandExecutor;
import megia.service.LocalStorageService;
import megia.service.LocalizationService;
import megia.service.TaskService;

/**
 * Performs a desktop-capable smoke test of the JavaFX chatbot shell.
 */
@Tag("gui")
public final class GuiSmokeTest {
    private static final long FX_TIMEOUT_SECONDS = 10;

    /**
     * Starts the JavaFX toolkit once for the GUI test class.
     */
    @BeforeAll
    public static void startJavaFxToolkit() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException exception) {
            // The toolkit is already running when the test suite shares a JVM with another GUI test.
        }
    }

    /**
     * Loads FXML, submits a command, renders a card, and refreshes controls after a language switch.
     *
     * @param temporaryDirectory Isolated directory used for task persistence.
     * @throws Exception If the JavaFX operation or assertion fails.
     */
    @Test
    public void loadsShellAndRefreshesLanguageAndTranscript(@TempDir Path temporaryDirectory)
            throws Exception {
        LocalizationService.setLanguage("en");
        runOnJavaFxThread(() -> {
            Path storagePath = temporaryDirectory.resolve("tasks.csv");
            CommandExecutor commandExecutor = new CommandExecutor(
                    new TaskService(new TaskStorage(), new LocalStorageService(storagePath.toString())));
            MainController controller = new MainController(
                    commandExecutor, null, new ProfileImageService());
            FXMLLoader loader = new FXMLLoader(
                    GuiSmokeTest.class.getResource("/megia/ui/MainView.fxml"));
            loader.setControllerFactory(type -> {
                if (type == MainController.class) {
                    return controller;
                }
                throw new IllegalStateException("Unexpected FXML controller: " + type.getName());
            });

            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    GuiSmokeTest.class.getResource("/megia/ui/chat.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            root.applyCss();
            root.layout();

            Button sendButton = (Button) loader.getNamespace().get("sendButton");
            TextField commandInput = (TextField) loader.getNamespace().get("commandInput");
            ListView<?> transcript = (ListView<?>) loader.getNamespace().get("transcriptList");
            assertNotNull(sendButton);
            assertEquals("Send", sendButton.getText());

            LocalizationService.setLanguage("cn");
            assertEquals("发送", sendButton.getText());

            commandInput.setText("todo smoke test");
            controller.handleSend();
            assertEquals(3, transcript.getItems().size());
            assertFalse(root.lookupAll(".task-card").isEmpty());
            assertFalse(root.lookupAll(".task-actions").isEmpty());

            controller.dispose();
            stage.close();
            LocalizationService.setLanguage("en");
        });
    }

    private static void runOnJavaFxThread(ThrowingRunnable action) throws Exception {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                completed.countDown();
            }
        });
        assertTrue(completed.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        if (failure.get() != null) {
            throw new AssertionError("JavaFX smoke test failed", failure.get());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
