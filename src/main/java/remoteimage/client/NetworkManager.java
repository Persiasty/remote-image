package remoteimage.client;

import remoteimage.shared.Configuration;
import remoteimage.shared.Item;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkManager {
    private String ip = "127.0.0.1";
    private ScheduledExecutorService threads = Executors.newScheduledThreadPool(2);

    private Future<?> broadcastTask;

    @FunctionalInterface
    public interface OnStatusChangeAction {
        void onStatusChange(Status status);
    }

    private OnStatusChangeAction listener = null;

    private class SendTask implements Runnable {
        private Item item;

        SendTask(Item item) {
            this.item = item;
        }

        @Override
        public void run() {
            Socket sock = null;
            try {
                sock = new Socket(ip, Configuration.SENDING_PORT);
                if (listener != null) {
                    listener.onStatusChange(Status.Sending);
                }

                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());

                oos.writeObject(item);
                oos.flush();

                if (listener != null) {
                    listener.onStatusChange(Status.Waiting);
                }

                final Socket copySocket = sock;
                Future task = threads.submit(() -> {
                    try {
                        if (listener != null) {
                            listener.onStatusChange(ois.readInt() == item.data.length ? Status.Sent : Status.Error);
                            threads.schedule(() -> listener.onStatusChange(Status.Ready), 3, TimeUnit.SECONDS);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onStatusChange(Status.Timeout);
                        }
                    } finally {
                        try { copySocket.close(); } catch (IOException ignored) { }
                    }
                });
                threads.schedule(() -> task.cancel(true), 10, TimeUnit.SECONDS);
            } catch (IOException ex) {
                if (listener != null) {
                    listener.onStatusChange(Status.Error);
                }
                ex.printStackTrace();
                try { if(sock != null) sock.close(); } catch (IOException ignored) { }
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
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FunctionalInterface
    public interface DiscoveryListener {
        public void onFind(InetAddress address);
    }

    public NetworkManager() {
        System.setProperty("http.agent", "Wget/1.9.1");
        try {
            ip = new String(Files.readAllBytes(Paths.get("ip.txt")), Charset.forName("UTF-8"));
        } catch (IOException e) { }
    }

    public void setDestinationAddress(InetAddress address) {
        if (address != null) ip = address.getHostAddress();
    }

    public void setOnStatusChangeAction(OnStatusChangeAction action) {
        listener = action;
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
