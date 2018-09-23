package remoteimage.server.view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import remoteimage.server.ImageCell;
import remoteimage.server.NetworkManager;
import remoteimage.shared.Item;
import remoteimage.shared.WithStage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

public class Controller implements WithStage {
    @FXML
    public MenuBar menuBar;
    @FXML
    public ListView<Image> imgsList;
    @FXML
    private ImageView image;

    private NetworkManager netman;
    private Stage currentStage;
    private int idx = 0;

    @FXML
    public void initialize() {
        netman = new NetworkManager();
        netman.setOnImageGetListener(this::addItem);

        image.fitWidthProperty().bind(((BorderPane)image.getParent()).widthProperty().subtract(imgsList.widthProperty()));
        image.fitHeightProperty().bind(((BorderPane)image.getParent()).heightProperty().subtract(menuBar.heightProperty()));

        imgsList.setCellFactory(p -> new ImageCell());

        MultipleSelectionModel<Image> selectionModel = imgsList.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.getSelectedItems().addListener((ListChangeListener<Image>) c ->
                Platform.runLater(() -> image.setImage(imgsList.getSelectionModel().getSelectedItem()))
        );
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

    private void addItem(Image img) {
        ObservableList<Image> list = imgsList.getItems();
        Platform.runLater(() ->{
            list.add(0, img);
            if(list.size() > 10)
                list.remove(10);
            imgsList.getSelectionModel().select(0);
            imgsList.refresh();
        });
    }

    public void fullScreen(MouseEvent mouseEvent) {
        currentStage.setFullScreen(!currentStage.isFullScreen());
    }

    @Override
    public void setupStage(Stage stage) {
        this.currentStage = stage;
        currentStage.setOnCloseRequest(e -> netman.shutdown());
    }
}
