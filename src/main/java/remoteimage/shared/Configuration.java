package remoteimage.shared;

import java.nio.charset.Charset;

public class Configuration {
    public static final int SENDING_PORT = 8080;
    public static final int BROADCASTING_PORT = 8888;
    public static final byte[] BROADCAST_STRING = "_DISCOVERY".getBytes(Charset.forName("UTF-8"));
}
