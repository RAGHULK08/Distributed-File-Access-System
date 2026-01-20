// Config.java
import java.io.*;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();
    
    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
        } catch (IOException e) {
            // Default values
            props.setProperty("index.host", "localhost");
            props.setProperty("index.port", "9090");
            props.setProperty("download.dir", "downloads");
            props.setProperty("buffer.size", "4096");
        }
    }
    
    public static String getIndexHost() {
        return props.getProperty("index.host");
    }
    
    public static int getIndexPort() {
        return Integer.parseInt(props.getProperty("index.port"));
    }
    
    public static String getDownloadDir() {
        return props.getProperty("download.dir");
    }
    
    public static int getBufferSize() {
        return Integer.parseInt(props.getProperty("buffer.size"));
    }
}