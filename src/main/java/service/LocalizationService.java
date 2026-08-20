package service;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class LocalizationService {
    private static final Properties prop = PropertiesService.getProperties();

    private static final ResourceBundle messages = ResourceBundle.getBundle(
            "i18n.messages.messages",
            Locale.of(prop.getProperty("language"))
    );

    private static final ResourceBundle exceptions = ResourceBundle.getBundle(
            "i18n.exceptions.exceptions",
            Locale.of(prop.getProperty("language"))
    );

    public static String getException(String key) { return exceptions.getString(key); }

    public static String getMessage(String key) {
        return messages.getString(key);
    }
}