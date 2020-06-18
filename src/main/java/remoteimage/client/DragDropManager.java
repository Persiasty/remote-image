package remoteimage.client;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import remoteimage.shared.Item;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DragDropManager {
    private Node dropArea, label;

    private ExecutorService bgTask = Executors.newSingleThreadExecutor();
    private FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
    private DropListener onDroppedAction;

    public interface DropListener {
        public void onDrop(Item item);
    }

    public DragDropManager(Node dropArea, Node label) {
        this.dropArea = dropArea;
        this.label = label;

        dropArea.setOnDragOver(this::onDragOver);
        dropArea.setOnDragEntered(this::onEntered);
        dropArea.setOnDragExited(this::onExited);
        dropArea.setOnDragDropped(this::onDrop);
    }

    private void onEntered(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() || db.hasHtml()) {
            label.getStyleClass().remove("drop-out");
            label.getStyleClass().add("drop-in");
        }
        event.consume();
    }

    private void onExited(DragEvent event) {
        label.getStyleClass().remove("drop-in");
        label.getStyleClass().add("drop-out");
        event.consume();
    }

    private void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() || db.hasHtml())
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        event.consume();
    }

    private void onDrop(DragEvent event) {
        BufferedInputStream target = getTarget(event.getDragboard());
        if (target != null) {
            bgTask.submit(() -> {
                try {
                    Item item = new Item();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buff = new byte[1024 * 512];
                    int len;
                    while ((len = target.read(buff)) != -1)
                        baos.write(buff, 0, len);

                    item.data = baos.toByteArray();
                    item.id = UUID.nameUUIDFromBytes(item.data).toString();

                    if(!Thread.interrupted() && onDroppedAction != null) onDroppedAction.onDrop(item);
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait());
                }
            });
            event.setDropCompleted(true);
        }
        event.consume();
    }

    private BufferedInputStream getTarget(Dragboard db) {
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            if (files.size() == 1 && imageFilter.accept(files.get(0))) {
                try {
                    return new BufferedInputStream(new FileInputStream(files.get(0)));
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait());
                }
            } else {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Niepoprawny format pliku", ButtonType.OK).showAndWait());
            }
        } else if (db.hasHtml()) {
            Document part = Jsoup.parseBodyFragment(db.getHtml());
            Elements imgs = part.getElementsByTag("img");
            if(imgs.size() > 0) {
                String src = imgs.first().attr("src");
                try {
                    return new BufferedInputStream(new URL(src).openStream());
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.toString(), ButtonType.OK).showAndWait());
                }
            } else {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Brak obrazu", ButtonType.OK).showAndWait());
            }
        }
        return null;
    }

    public void setOnDroppedAction(DropListener onDroppedAction) {
        this.onDroppedAction = onDroppedAction;
    }

    public void shutdown() {
        bgTask.shutdownNow();
    }
}
