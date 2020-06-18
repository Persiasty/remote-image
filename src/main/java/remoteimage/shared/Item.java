package remoteimage.shared;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 2L;
    public byte[] data;
    public String id;
    public RequestType type;

    public Item() {
        this(RequestType.REQ_UPLOAD);
    }

    public Item(RequestType type) {
        this.type = type;
        this.data = new byte[0];
        this.id = "";
    }
}
