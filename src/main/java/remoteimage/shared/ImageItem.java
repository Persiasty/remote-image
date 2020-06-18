package remoteimage.shared;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageItem {
    public String id;
    public Image image;

    public static ImageItem fromItem(Item item) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(item.data));
        return new ImageItem(item.id, SwingFXUtils.toFXImage(img, null));
    }
    public ImageItem(String id, Image image) {
        this.id = id;
        this.image = image;
    }
}
