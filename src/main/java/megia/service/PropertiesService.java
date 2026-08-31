package megia.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application configuration with safe defaults for optional settings.
 */
public final class PropertiesService {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_FILE_PATH = "./task_storage.csv";

    private PropertiesService() {
    }

    /**
     * Loads application properties, defaulting to English if loading fails.
     *
     * @return Loaded application properties.
     */
    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("language", DEFAULT_LANGUAGE);
        try (InputStream inputStream = PropertiesService.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (inputStream == null) {
                System.err.println("Can't find application.properties. Using default settings.");
                return properties;
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            System.err.printf("Can't read application.properties. Using default settings: %s%n",
                    exception.getMessage());
            properties.setProperty("language", DEFAULT_LANGUAGE);
            properties.setProperty("storage.task.path", DEFAULT_FILE_PATH);
        }
        return properties;
    }
}
