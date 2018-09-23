package remoteimage.client.view;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import remoteimage.client.DragDropManager;
import remoteimage.client.NetworkManager;
import remoteimage.shared.WithStage;

import java.net.InetAddress;

public class Controller implements WithStage {
    @FXML
    public BorderPane dropArea;
    @FXML
    public Label label;

    private Stage currentStage;
    private NetworkManager netman;
    private DragDropManager ddman;

    @FXML
    public void initialize(){
        netman = new NetworkManager();
        ddman = new DragDropManager(dropArea, label);
        ddman.setOnDroppedAction(netman::sendItem);
    }

    @Override
    public void setupStage(Stage stage) {
        currentStage = stage;
        currentStage.setOnCloseRequest(e -> {
            netman.shutdown();
            ddman.shutdown();
        });
    }

    @FXML
    public void discoverIp(MouseEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("IP Discovery Service");
        alert.setHeaderText("IP Discovery Service");
        alert.setContentText("Scroll to find display device");

        ListView<InetAddress> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        netman.startDiscovery(address -> {
            ObservableList<InetAddress> list = listView.getItems();
            if(!list.contains(address)) {
                Platform.runLater(() -> list.add(address));
            }
        });

        listView.setMaxWidth(Double.MAX_VALUE);
        listView.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(listView, Priority.ALWAYS);
        GridPane.setHgrow(listView, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(listView, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        currentStage.setAlwaysOnTop(false);

        alert.showAndWait();

        currentStage.setAlwaysOnTop(true);

        netman.stopDiscovery();
        netman.setDestinationAddress(listView.getSelectionModel().getSelectedItem());
    }
}
