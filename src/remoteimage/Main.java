package remoteimage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import remoteimage.server.view.Controller;
import remoteimage.shared.WithStage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene;
        FXMLLoader loader;
        if(getParameters().getUnnamed().contains("server")) {
            primaryStage.setTitle("Display");
            primaryStage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));

            loader = new FXMLLoader(getClass().getResource("server/view/server.fxml"));
            scene = new Scene(loader.load(), 1080, 720);
        } else {
            primaryStage.setTitle("Share");
            primaryStage.setAlwaysOnTop(true);

            loader = new FXMLLoader(getClass().getResource("client/view/client.fxml"));
            scene = new Scene(loader.load(), 300, 150);
        }

        WithStage controller = loader.getController();
        controller.setupStage(primaryStage);

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
