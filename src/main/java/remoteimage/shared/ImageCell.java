package remoteimage.shared;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ImageCell extends ListCell<ImageItem> {

    @Override
    protected void updateItem(ImageItem item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty && item != null) {
            ImageView view = new ImageView();
            view.setPreserveRatio(true);
            view.setFitHeight(100);
            view.setFitWidth(100);
            view.setImage(item.image);
            setGraphic(view);
            setAlignment(Pos.CENTER);
        } else setGraphic(null);
    }
}
