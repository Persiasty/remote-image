package remoteimage.server;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class ImageCell extends ListCell<BufferedImage> {

    @Override
    protected void updateItem(BufferedImage item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty && item != null) {
            ImageView view = new ImageView();
            view.setPreserveRatio(true);
            view.setFitHeight(100);
            view.setFitWidth(100);
            view.setImage(SwingFXUtils.toFXImage(item, null));
            setGraphic(view);
        } else setGraphic(null);
    }
}
