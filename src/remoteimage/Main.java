package remoteimage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import remoteimage.server.Controller;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene;
        if(getParameters().getUnnamed().contains("server")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("server/server.fxml"));
            Parent root = loader.load();
            Controller controller = loader.getController();
            controller.setStageAndSetupListeners(primaryStage);
            primaryStage.setTitle("Display");
            scene = new Scene(root, 800, 600);
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("client/client.fxml"));
            primaryStage.setTitle("Share");
            primaryStage.setAlwaysOnTop(true);
            scene = new Scene(root, 200, 200);
        }

        primaryStage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.ESCAPE));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
