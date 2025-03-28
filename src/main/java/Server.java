import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Server {
    private static final int PORT = 9120;
    private static final int MAX_SLAVES = 2;
    private static final int TIMEOUT = 10000; // 10 Sekunden
    private static final List<Socket> slaves = new ArrayList<>();

    public static void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Master wartet auf Slaves...");
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TIMEOUT && slaves.size() < MAX_SLAVES) {
                serverSocket.setSoTimeout(TIMEOUT);
                try {
                    Socket slaveSocket = serverSocket.accept();
                    synchronized (slaves) {
                        slaves.add(slaveSocket);
                        System.out.println("Slave registriert: " + slaveSocket.getInetAddress());
                    }
                } catch (SocketTimeoutException ignored) {
                }
            }

            if (!slaves.isEmpty()) {
                distributeTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void distributeTasks() {
        Random rand = new Random();
        for (Socket slave : slaves) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(slave.getOutputStream());
                byte[] data = new byte[10];
                rand.nextBytes(data);
                Message msg = new Message("Exercise", 0, data);
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        receiveResults();
    }

    private static void receiveResults() {
        for (Socket slave : slaves) {
            try {
                ObjectInputStream in = new ObjectInputStream(slave.getInputStream());
                Message msg = (Message) in.readObject();
                System.out.println("Result von " + slave.getInetAddress() + ": " + Arrays.toString(msg.data));
                slave.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
