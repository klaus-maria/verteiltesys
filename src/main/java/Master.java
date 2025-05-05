import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.IntStream;

class Master implements Runnable{
    private Map<Integer, Socket> slaves = new HashMap<>();
    private Map<Integer, ObjectInputStream> inStreams = new HashMap<>();
    private Map<Integer, ObjectOutputStream> outStreams = new HashMap<>();
    private Map<Integer, ArrayList> tasks = new HashMap<>();
    private Map<Integer, int []> positions = new HashMap<>();
    private ArrayList<Integer> recieved = new ArrayList<>();
    private Map<int[], Object> results = new HashMap<>();
    private static int port;
    private static int maxWorkers;
    private static int timeout;

    private static int[][] matrixA;
    private static int[][] matrixB;
    private final int[][] resultMatrix = new int[matrixA.length][matrixB[0].length];

    public static void config(int p, int mW, int t, int[][] a, int[][] b){
        port = p;
        maxWorkers = mW;
        timeout = t;
        matrixA = a;
        matrixB = b;
    }

    public static void spawn(){
        Master m = new Master();
        Thread t = new Thread(m);
        t.start();
    }
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Master wartet auf Slaves...");
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout && slaves.size() < maxWorkers) {
                serverSocket.setSoTimeout(timeout);
                try {
                    Socket slaveSocket = serverSocket.accept();
                    synchronized (slaves) {
                        ObjectInputStream in = new ObjectInputStream(slaveSocket.getInputStream());
                        Message register = (Message) in.readObject();
                        slaves.put(register.slaveId, slaveSocket);
                        inStreams.put(register.slaveId, in);
                        System.out.println("Slave " + register.slaveId + " registriert: " + slaveSocket.getInetAddress());
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!slaves.isEmpty()) {
                sendPeers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPeers(){
        // send slaveIds to each slave -> mesh network
        for(int slaveId: slaves.keySet()){
            Socket slave = slaves.get(slaveId);
            try {
                ObjectOutputStream out = new ObjectOutputStream(slave.getOutputStream());
                ArrayList ids = new ArrayList(slaves.keySet());
                Message msg = new Message("Connections", slaveId, null, ids);
                out.writeObject(msg);
                out.flush();
                outStreams.put(slaveId, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        distributeTasks();
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
                    ObjectOutputStream out = outStreams.get(slaveId);
                    ArrayList task = packageTask(matrixA, matrixB, pos);
                    Message msg = new Message("Exercise", slaveId, pos, task);
                    out.writeObject(msg);
                    out.flush();
                    tasks.put(slaveId, task);
                    positions.put(slaveId, pos);
                    System.out.println("sent: ID: " + slaveId + " Task: " + task);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                slaveId++;

            }
        }
        receiveResults();
    }

    private void receiveResults() {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < timeout && results.size() != tasks.size()){ //check until time runs out
            for (int slaveId : slaves.keySet()) {
                try {
                    if(!recieved.contains(slaveId)) { //only check if not already recieved
                        Socket slave = slaves.get(slaveId);
                        ObjectInputStream in = inStreams.get(slaveId);
                        Message msg = (Message) in.readObject();
                        System.out.println("recieved: " + msg.slaveId);
                        results.put(msg.pos, msg.data);
                        recieved.add(slaveId);
                        slave.close();
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        checkResults();
    }

    private void checkResults(){
        // check if all slaveIds in recieved arralist
        for(Integer x: slaves.keySet()){
            if(!recieved.contains(x)){
                System.out.println("missing: " + x);
                // assign task newly to other member
                Random rand = new Random();
                Integer newId = rand.nextInt(Collections.max(recieved) - Collections.min(recieved)) + Collections.min(recieved);
                ObjectOutputStream newAssignee = outStreams.get(newId);
                ArrayList oldTask = tasks.get(x);
                int[] oldPos = positions.get(x);
                Message newTask = new Message("Exercise", newId, oldPos,oldTask);
                try {
                    newAssignee.writeObject(newTask);
                    newAssignee.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // remove other memeber from recieved ???
                //remove task, pos, slaveID, in, out of old?
                recieved.remove(newId);
                // receiveResults again
                receiveResults();
            }
        }
        combineResults();
    }

    private void combineResults() {
        System.out.println("combining results");
        for (int[] pos : results.keySet()) {
            int y = pos[1];
            int x = pos[0];
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