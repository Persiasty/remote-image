package remoteimage.client;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import remoteimage.shared.Item;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class Controller {

    private String ip = "127.0.0.1";

    @FXML
    public BorderPane borderPane;
    @FXML
    public Label dropArea;
    FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());

    @FXML
    public void initialize(){
        borderPane.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if(getTarget(event.getDragboard()) != null)
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        });
        borderPane.setOnDragEntered(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if(getTarget(event.getDragboard()) != null) {
                    dropArea.getStyleClass().clear();
                    dropArea.getStyleClass().add("dropin");
                }
                event.consume();
            }
        });
        borderPane.setOnDragExited(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                dropArea.getStyleClass().clear();
                dropArea.getStyleClass().add("dropout");
                event.consume();
            }
        });
        borderPane.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                BufferedInputStream target = getTarget(event.getDragboard());
                if(target != null) {
                    new Thread(() -> {
                        try {
                            Item item = new Item();

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buff = new byte[1024 * 512];
                            int len = 0;
                            while((len = target.read(buff)) != -1)
                                baos.write(buff, 0, len);

                            item.data = baos.toByteArray();

                            Socket sock = new Socket(ip, 8080);
                            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                            oos.writeObject(item);
                            oos.flush();
                            sock.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    event.setDropCompleted(true);
                }
                event.consume();
            }
        });
    }

    private BufferedInputStream getTarget(Dragboard db) {
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            if (files.size() == 1 && imageFilter.accept(files.get(0))) {
                try {
                    return new BufferedInputStream(new FileInputStream(files.get(0)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (db.hasUrl()) {
            try {
                return new BufferedInputStream(new URL(db.getUrl()).openStream());
            } catch (IOException e) { e.printStackTrace(); }
        }
        return null;
    }

    @FXML
    public void setIp(MouseEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Set Server IP Address");
        dialog.setHeaderText("Set Server IP Address");
        dialog.setContentText("IP: ");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> this.ip = ip);
    }
}
