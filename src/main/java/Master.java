import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

class Master {
    private final List<Socket> slaves = new ArrayList<>();
    private final Map<Integer, int[]> results = new HashMap<>();
    private int slaveCounter = 1;

    private final int[][] matrixA = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
    };
    private final int[][] matrixB = {
            {9, 8, 7},
            {6, 5, 4},
            {3, 2, 1}
    };
    private final int[][] resultMatrix = new int[matrixA.length][matrixB[0].length];

    public void start(int port, int maxSlaves, int timeout) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master wartet auf Slaves...");
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout && slaves.size() < maxSlaves) {
                serverSocket.setSoTimeout(timeout);
                try {
                    Socket slaveSocket = serverSocket.accept();
                    synchronized (slaves) {
                        slaves.add(slaveSocket);
                        ObjectOutputStream out = new ObjectOutputStream(slaveSocket.getOutputStream());
                        Message initMsg = new Message("Initialize", slaveCounter, -1, new int[0]);
                        out.writeObject(initMsg);
                        out.flush();
                        System.out.println("Slave " + slaveCounter + " registriert: " + slaveSocket.getInetAddress());
                        slaveCounter++;
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
    private void distributeTasks() {
        int row = 0;
        for (Socket slave : slaves) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(slave.getOutputStream());
                Message msg = new Message("Exercise", row + 1, row, matrixA[row]);
                out.writeObject(msg);
                out.flush();
                row++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        receiveResults();
    }

    private void receiveResults() {
        for (Socket slave : slaves) {
            try {
                ObjectInputStream in = new ObjectInputStream(slave.getInputStream());
                Message msg = (Message) in.readObject();
                results.put(msg.row, msg.data);
                slave.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        combineResults();
    }

    private void combineResults() {
        for (int row : results.keySet()) {
            resultMatrix[row] = results.get(row);
        }
        System.out.println("Ergebnis der Matrixmultiplikation:");
        for (int[] row : resultMatrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}