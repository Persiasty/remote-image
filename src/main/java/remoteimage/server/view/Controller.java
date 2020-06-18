package remoteimage.server.view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import remoteimage.shared.ImageCell;
import remoteimage.server.NetworkManager;
import remoteimage.shared.ImageItem;
import remoteimage.shared.WithStage;

public class Controller implements WithStage, NetworkManager.OnImageGetListener {
    @FXML
    public MenuBar menuBar;
    @FXML
    public ListView<ImageItem> imgsList;
    @FXML
    private ImageView image;

    private NetworkManager netman;
    private Stage currentStage;
    private int idx = 0;

    @FXML
    public void initialize() {
        netman = new NetworkManager();
        netman.setOnImageGetListener(this);

        image.fitWidthProperty().bind(((BorderPane)image.getParent()).widthProperty().subtract(imgsList.widthProperty()));
        image.fitHeightProperty().bind(((BorderPane)image.getParent()).heightProperty().subtract(menuBar.heightProperty()));

        imgsList.setCellFactory(p -> new ImageCell());

        MultipleSelectionModel<ImageItem> selectionModel = imgsList.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.getSelectedItems().addListener((ListChangeListener<ImageItem>) c -> {
            if (c.getList().size() < 1) Platform.runLater(() -> imgsList.getSelectionModel().select(0));
            else Platform.runLater(() -> image.setImage(c.getList().get(0).image));
        });
    }

    @FXML
    public void broadcastIp(MouseEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Broadcasting IP");
        alert.setHeaderText("Broadcasting IP");

        netman.startBroadcast();
        alert.showAndWait();
        netman.stopBroadcast();
    }

    public void fullScreen(MouseEvent mouseEvent) {
        currentStage.setFullScreen(!currentStage.isFullScreen());
    }

    @Override
    public void setupStage(Stage stage) {
        this.currentStage = stage;
        currentStage.setOnCloseRequest(e -> netman.shutdown());
    }

    @Override
    public void onGetImage(ImageItem img) {
        Platform.runLater(() ->{
            imgsList.getItems().add(0, img);
//            if(list.size() > 10)
//                list.remove(10);
            imgsList.getSelectionModel().select(0);
            imgsList.refresh();
        });
    }

    @Override
    public void onRemoveImage(String id) {
        Platform.runLater(() -> {
            imgsList.getItems().removeIf(i -> i.id.equals(id));
//            if(list.size() > 10)
//                list.remove(10);
            imgsList.getSelectionModel().select(0);
            imgsList.refresh();
        });
    }
}
