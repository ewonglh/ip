package service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesService {
    public static Properties getProperties() {
        Properties prop = new Properties();
        try (InputStream in = PropertiesService.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                System.err.println("Can't find application.properties");
            }
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}