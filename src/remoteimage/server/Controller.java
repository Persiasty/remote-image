package remoteimage.server;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import remoteimage.shared.Item;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

public class Controller {
    @FXML
    public MenuBar menuBar;
    @FXML
    public ListView<BufferedImage> imgsList;
    @FXML
    private ImageView image;

    private Stage stage;
    private int idx = 0;


    private ServerSocket ssock;
    private Thread thSock = new Thread(() -> {
        try {
            ssock = new ServerSocket(8080);
            ssock.setSoTimeout(1000);
            do {
                try {
                    Socket sock = ssock.accept();
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));
                    Item item = (Item) ois.readObject();
                    sock.close();

                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(item.data));
                    addItem(img);
                    Platform.runLater(() -> {
                        image.setImage(SwingFXUtils.toFXImage(img, null));
                    });
                } catch(SocketTimeoutException ignored) { }
            } while (!Thread.interrupted());
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    @FXML
    public void initialize() throws IOException {
        image.fitWidthProperty().bind(((BorderPane)image.getParent()).widthProperty().subtract(imgsList.widthProperty()));
        image.fitHeightProperty().bind(((BorderPane)image.getParent()).heightProperty().subtract(menuBar.heightProperty()));
        imgsList.setCellFactory(new Callback<ListView<BufferedImage>, ListCell<BufferedImage>>() {
            @Override
            public ListCell<BufferedImage> call(ListView<BufferedImage> param) {
                return new ImageCell();
            }
        });
        MultipleSelectionModel<BufferedImage> selectionModel = imgsList.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        selectionModel.getSelectedItems().addListener(new ListChangeListener<BufferedImage>() {
            @Override
            public void onChanged(Change<? extends BufferedImage> c) {
                Platform.runLater(() -> {
                    image.setImage(SwingFXUtils.toFXImage(c.getList().get(0), null));
                });
            }
        });
    }

    public void setStageAndSetupListeners(Stage stage) {
        thSock.start();
        this.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                thSock.interrupt();
            }
        });
    }
    @FXML
    public void showInfo(MouseEvent actionEvent) throws IOException {
        Process proc = Runtime.getRuntime().exec("ipconfig");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("IP Config");
        alert.setHeaderText("IP Config info");
        alert.setContentText("Scroll to find important information");

        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        TextArea textArea = new TextArea(reader.lines().reduce((s1, s2) -> s1 + "\r\n" + s2).orElse("Failed"));
        textArea.setEditable(false);
        textArea.setWrapText(true);

        reader.close();

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }

    private void addItem(BufferedImage img) {
        ObservableList<BufferedImage> list = imgsList.getItems();
        Platform.runLater(() ->{
            list.add(0, img);
            if(list.size() > 10)
                list.remove(10);
            idx = 0;
            imgsList.getSelectionModel().select(idx);
            imgsList.refresh();
        });
    }

    public void fullScreen(MouseEvent mouseEvent) {
        stage.setFullScreen(true);
    }
}
