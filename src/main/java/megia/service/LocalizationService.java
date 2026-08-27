package megia.service;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import megia.exception.ErrorCode;

/**
 * Loads and formats localized text, falling back to English when possible.
 */
public final class LocalizationService {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final ResourceBundle.Control BUNDLE_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
    private static final Properties PROPERTIES = PropertiesService.getProperties();
    private static final Locale LOCALE = resolveLocale(PROPERTIES.getProperty("language"));
    private static final ResourceBundle ENGLISH_MESSAGES = loadBundle(
            "i18n.messages.messages", Locale.of(DEFAULT_LANGUAGE));
    private static final ResourceBundle ENGLISH_EXCEPTIONS = loadBundle(
            "i18n.exceptions.exceptions", Locale.of(DEFAULT_LANGUAGE));
    private static final ResourceBundle MESSAGES = loadBundle("i18n.messages.messages", LOCALE);
    private static final ResourceBundle EXCEPTIONS = loadBundle("i18n.exceptions.exceptions", LOCALE);

    private LocalizationService() {
    }

    /**
     * Formats the localized message associated with a structured error.
     *
     * @param errorCode Error whose message should be retrieved.
     * @param arguments Values interpolated into the message.
     * @return Formatted localized error message.
     */
    public static String getException(ErrorCode errorCode, Object... arguments) {
        String template = getLocalizedValue(EXCEPTIONS, ENGLISH_EXCEPTIONS, errorCode.name());
        try {
            return String.format(LOCALE, template, arguments);
        } catch (IllegalFormatException exception) {
            System.err.printf("Invalid format for exception message '%s': %s%n",
                    errorCode, exception.getMessage());
            return template;
        }
    }

    /**
     * Returns the localized message associated with a message key.
     *
     * @param key Message key to retrieve.
     * @return Localized message text.
     */
    public static String getMessage(String key) {
        return getLocalizedValue(MESSAGES, ENGLISH_MESSAGES, key);
    }

    private static Locale resolveLocale(String language) {
        String configuredLanguage = language == null ? DEFAULT_LANGUAGE : language.strip();
        if (configuredLanguage.isEmpty()) {
            System.err.println("The configured language is empty. Using English.");
            return Locale.of(DEFAULT_LANGUAGE);
        }

        try {
            return Locale.of(configuredLanguage);
        } catch (RuntimeException exception) {
            System.err.printf("The configured language '%s' is invalid. Using English.%n",
                    configuredLanguage);
            return Locale.of(DEFAULT_LANGUAGE);
        }
    }

    private static ResourceBundle loadBundle(String baseName, Locale targetLocale) {
        try {
            return ResourceBundle.getBundle(baseName, targetLocale, BUNDLE_CONTROL);
        } catch (MissingResourceException exception) {
            if (!targetLocale.getLanguage().equals(DEFAULT_LANGUAGE)) {
                System.err.printf("Localization for '%s' is unavailable. Using English.%n",
                        targetLocale.getLanguage());
            } else {
                System.err.printf("Required English localization bundle '%s' is unavailable.%n", baseName);
            }
            return null;
        }
    }

    private static String getLocalizedValue(
            ResourceBundle selectedBundle,
            ResourceBundle fallbackBundle,
            String key) {
        if (selectedBundle != null && selectedBundle.containsKey(key)) {
            return selectedBundle.getString(key);
        }
        if (fallbackBundle != null && fallbackBundle.containsKey(key)) {
            System.err.printf("Localization key '%s' is missing; using English.%n", key);
            return fallbackBundle.getString(key);
        }

        System.err.printf("Localization key '%s' is missing from all bundles.%n", key);
        return "Missing message: " + key;
    }
}
