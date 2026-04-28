package core;

import javafx.scene.image.Image;
import java.net.URL;

public class FileBindings {
 
    public static final String defaultImage = "img/tests/lena_std.jpg";

    public static final URL GUIMain = FileBindings.class.getResource("/MainWindow.fxml");
    public static final Image favicon = new Image(FileBindings.class.getResourceAsStream("/favicon.png"));
}
