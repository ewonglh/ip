import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class Megia {

    public static Properties getProperties() {
        Properties prop = new Properties();
        try (InputStream in = Megia.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                System.err.println("Can't find application.properties");
            }
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static void main(String[] args) {
        Properties prop = getProperties();
        ResourceBundle messages = ResourceBundle.getBundle(
                "i18n.messages.messages",
                Locale.of(prop.getProperty("language"))
        );

        String banner = """
                /\\ "-./  \\   /\\  ___\\   /\\  ___\\   /\\ \\   /\\  __ \\
                \\ \\ \\-./\\ \\  \\ \\  __\\   \\ \\ \\__‾\\  \\ \\ \\  \\ \\  __ \\
                \\ \\_\\ \\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\  \\ \\_\\ \\_\\
                \\/_/  \\/_/   \\/_____/   \\/_____/   \\/_/   \\/_/\\/_/
                """.stripTrailing() + "\n\n";

        MessageSender.sendMessage(banner + messages.getString("greeting"));
        MessageSender.sendMessage(messages.getString("farewell"));
    }
}