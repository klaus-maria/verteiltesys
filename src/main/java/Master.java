import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.IntStream;

class Master {
    private final Map<Integer, Socket> slaves = new HashMap<>();
    private final Map<int[], Object> results = new HashMap<>();

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
                        ObjectInputStream in = new ObjectInputStream(slaveSocket.getInputStream());
                        Message register = (Message) in.readObject();
                        slaves.put(register.slaveId, slaveSocket);
                        System.out.println("Slave " + register.slaveId + " registriert: " + slaveSocket.getInetAddress());
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
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
        // check size of matrices
        if(!checkMatrices(matrixA, matrixB)) return;
        // iterate over vals in matrices/slaves
        int slaveId = 0;
        for(int y=0; y<matrixA.length; y++){
            for(int x=0; x<matrixA[0].length; x++){
                Socket slave = slaves.get(slaveId);
                int[] pos = new int[]{y,x};
                try {
                    ObjectOutputStream out = new ObjectOutputStream(slave.getOutputStream());
                    Message msg = new Message("Exercise", slaveId, pos, packageTask(matrixA, matrixB, pos));
                    out.writeObject(msg);
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                slaveId++;

            }
        }
        receiveResults();
    }

    private void receiveResults() {
        for (int slaveId : slaves.keySet()) {
            try {
                Socket slave = slaves.get(slaveId);
                ObjectInputStream in = new ObjectInputStream(slave.getInputStream());
                Message msg = (Message) in.readObject();
                results.put(msg.pos, msg.data);
                slave.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        combineResults();
    }

    private void combineResults() {
        for (int[] pos : results.keySet()) {
            int y = pos[0];
            int x = pos[1];
            resultMatrix[y][x] = (int) results.get(pos);
        }
        System.out.println("Ergebnis der Matrixmultiplikation:");
        for (int[] row : resultMatrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    private int[] getColumn(int[][] matrix, int column) {
        return Arrays.stream(matrix).mapToInt(ints -> ints[column]).toArray();
    }

    private ArrayList packageTask(int[][] matrixA, int[][] matrixB, int[] pos){
        ArrayList task = new ArrayList();
        int[] column = getColumn(matrixB, pos[0]);
        int[] row = matrixA[pos[1]];
        task.add(column);
        task.add(row);
        return task;
    }

    private boolean checkMatrices(int[][] a, int[][] b){
        if(a.length == b[0].length) return true;
        return false;
    }
}