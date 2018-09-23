package remoteimage.server;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import remoteimage.shared.Configuration;
import remoteimage.shared.Item;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkManager {
    private ScheduledExecutorService threads = Executors.newScheduledThreadPool(2);
    private Future<?> serverTask, broadcastTask;
    private OnImageGetListener onImageGetListener;

    public interface OnImageGetListener {
        public void onGetImage(Image img);
    }

    private class ServerTask implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                serverSocket.setSoTimeout(1000);
                do {
                    try {
                        Socket sock = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                        Item item = (Item) ois.readObject();
                        sock.close();

                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(item.data));
                        onImageGetListener.onGetImage(SwingFXUtils.toFXImage(img, null));
                    } catch (SocketTimeoutException | ClassNotFoundException ignored) { }
                } while (!Thread.interrupted());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class BroadcastTask implements Runnable {
        private List<InetAddress> bras;

        BroadcastTask(List<InetAddress> bras) {
            this.bras = bras;
        }

        @Override
        public void run() {
            try (DatagramSocket sock = new DatagramSocket()) {
                for (InetAddress address : bras) {
                    sock.send(new DatagramPacket(
                            Configuration.BROADCAST_STRING,
                            Configuration.BROADCAST_STRING.length,
                            address, Configuration.BROADCASTING_PORT
                    ));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public NetworkManager() {
        serverTask = threads.submit(new ServerTask());
    }

    private List<InetAddress> getAllBroadcast() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null)
                        continue;
                    addresses.add(broadcast);
                }
            }
        } catch (SocketException ignored) { }
        return addresses;
    }


    public void startBroadcast() {
        if (broadcastTask == null) {
            broadcastTask = threads.scheduleAtFixedRate(new BroadcastTask(getAllBroadcast()), 0, 1, TimeUnit.SECONDS);
        }
    }

    public void stopBroadcast() {
        if (broadcastTask != null) {
            broadcastTask.cancel(true);
            broadcastTask = null;
        }
    }

    public void setOnImageGetListener(OnImageGetListener onImageGetListener) {
        this.onImageGetListener = onImageGetListener;
    }

    public void shutdown() {
        serverTask.cancel(true);
        threads.shutdownNow();
    }

}
