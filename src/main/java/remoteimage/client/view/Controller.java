package remoteimage.client.view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import remoteimage.client.DragDropManager;
import remoteimage.client.NetworkManager;
import remoteimage.client.Status;
import remoteimage.shared.*;

import java.io.IOException;
import java.net.InetAddress;

public class Controller implements WithStage {
    @FXML
    public BorderPane dropArea;
    @FXML
    public Label label;
    @FXML
    public Label status;
    @FXML
    public ListView<ImageItem> images;
    @FXML
    public Button btRemove;

    private Stage currentStage;
    private NetworkManager netman;
    private DragDropManager ddman;

    @FXML
    public void initialize(){
        netman = new NetworkManager();
        netman.setOnStatusChangeAction(this::onStatusChange);

        ddman = new DragDropManager(dropArea, label);
        ddman.setOnDroppedAction(this::droppedAction);

        images.setCellFactory(p -> new ImageCell());
        MultipleSelectionModel<ImageItem> selectionModel = images.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.getSelectedItems().addListener(
                (ListChangeListener<ImageItem>) c -> Platform.runLater(() -> {
                    ImageItem selected = images.getSelectionModel().getSelectedItem();
                    btRemove.setDisable(selected == null);
                })
        );
    }

    private void droppedAction(Item item) {
        netman.sendItem(item);
        try {
            ImageItem it = ImageItem.fromItem(item);
            Platform.runLater(() -> {
                images.getItems().add(0, it);
                images.refresh();
            });
        } catch (IOException e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait());
        }
    }

    public void onStatusChange(Status status) {
        Platform.runLater(() -> this.status.setText(status.name()));
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

    public void removeAction(ActionEvent actionEvent) {
        ImageItem selected = images.getSelectionModel().getSelectedItem();

        Item it = new Item(RequestType.REQ_REMOVE);
        it.id = selected.id;

        netman.sendItem(it);
        images.getItems().removeAll(selected);
    }
}
