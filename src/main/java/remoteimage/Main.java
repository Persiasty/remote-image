package remoteimage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import remoteimage.shared.WithStage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene;
        FXMLLoader loader;
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        if(getParameters().getUnnamed().contains("server")) {
            primaryStage.setTitle("Display");
            primaryStage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));

            loader = new FXMLLoader(cl.getResource("server/server.fxml"));
            scene = new Scene(loader.load(), 1080, 720);
        } else {
            primaryStage.setTitle("Share");
            primaryStage.setAlwaysOnTop(true);

            loader = new FXMLLoader(cl.getResource("client/client.fxml"));
            scene = new Scene(loader.load(), 200, 300);
        }

        WithStage controller = loader.getController();
        controller.setupStage(primaryStage);

        primaryStage.getIcons().add(new Image(cl.getResourceAsStream("icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
