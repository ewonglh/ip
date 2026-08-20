package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application configuration with safe defaults for optional settings.
 */
public final class PropertiesService {
    private static final String DEFAULT_LANGUAGE = "en";

    private PropertiesService() {
    }

    /**
     * Loads application properties, defaulting to English if loading fails.
     */
    public static Properties getProperties() {
        Properties prop = new Properties();
        prop.setProperty("language", DEFAULT_LANGUAGE);
        try (InputStream in = PropertiesService.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                System.err.println("Can't find application.properties. Using default settings.");
                return prop;
            }
            prop.load(in);
        } catch (IOException e) {
            System.err.printf("Can't read application.properties. Using default settings: %s%n", e.getMessage());
            prop.setProperty("language", DEFAULT_LANGUAGE);
        }
        return prop;
    }
}
