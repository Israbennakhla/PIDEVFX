import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import java.io.File;
import java.net.URL;

public class TestFxml {
    public static void main(String[] args) throws Exception {
        com.sun.javafx.application.PlatformImpl.startup(() -> {});
        File dir = new File("src/main/resources/com/sitmypet/fxml");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".fxml")) {
                try {
                    URL url = f.toURI().toURL();
                    FXMLLoader loader = new FXMLLoader(url);
                    loader.load();
                    System.out.println(f.getName() + ": OK");
                } catch (Exception e) {
                    System.out.println(f.getName() + ": ERROR - " + e.getMessage());
                }
            }
        }
        System.exit(0);
    }
}