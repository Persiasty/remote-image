package remoteimage.server;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageCell extends ListCell<Image> {

    @Override
    protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty && item != null) {
            ImageView view = new ImageView();
            view.setPreserveRatio(true);
            view.setFitHeight(100);
            view.setFitWidth(100);
            view.setImage(item);
            setGraphic(view);
        } else setGraphic(null);
    }
}
