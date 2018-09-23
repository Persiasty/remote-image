package remoteimage.client;

import remoteimage.shared.Configuration;
import remoteimage.shared.Item;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

public class NetworkManager {
    private String ip = "127.0.0.1";
    private ScheduledExecutorService threads = Executors.newScheduledThreadPool(2);

    private Future<?> broadcastTask;

    private class SendTask implements Runnable {
        private Item item;
        SendTask(Item item) {
            this.item = item;
        }
        @Override
        public void run() {
            try(Socket sock = new Socket(ip, Configuration.SENDING_PORT)) {
                try(ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream())) {
                    oos.writeObject(item);
                    oos.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class ReceiveTask implements Runnable {
        private DiscoveryListener listener;

        ReceiveTask(DiscoveryListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            try (DatagramSocket sock = new DatagramSocket(Configuration.BROADCASTING_PORT)) {
                sock.setSoTimeout(500);
                while (!Thread.interrupted()) {
                    byte[] data = new byte[Configuration.BROADCAST_STRING.length];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        sock.receive(packet);
                        if (Arrays.equals(Configuration.BROADCAST_STRING, packet.getData())) {
                            listener.onFind(packet.getAddress());
                        }
                    } catch(SocketTimeoutException ignored) { }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public interface DiscoveryListener {
        public void onFind(InetAddress address);
    }

    public NetworkManager() {
        System.setProperty("http.agent", "Wget/1.9.1");
    }

    public void setDestinationAddress(InetAddress address) {
        if(address != null) ip = address.getHostAddress();
    }


    public void sendItem(Item item) {
        threads.submit(new SendTask(item));
    }

    public void startDiscovery(DiscoveryListener listener) {
        if (broadcastTask == null) {
            broadcastTask = threads.submit(new ReceiveTask(listener));
        }
    }

    public void stopDiscovery() {
        if (broadcastTask != null) {
            broadcastTask.cancel(true);
            broadcastTask = null;
        }
    }

    public void shutdown() {
        threads.shutdownNow();
    }

}
