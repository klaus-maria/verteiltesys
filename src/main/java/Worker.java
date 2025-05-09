import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.stream.IntStream;

class Worker implements Runnable{

    private static int count = 0;
    private static String masterIp;
    private static int port;
    private ArrayList connections = new ArrayList();

    public static void config(String i, int p){
        masterIp = i;
        port = p;
    }

    public static void spawn(int count){
        for(int i=0; i<count; i++){
            Worker w = new Worker();
            Thread t = new Thread(w);
            t.start();
        }
    }

    public void run() {
        int slaveId = count;
        count++;

        boolean running = true;

        try (Socket socket = new Socket(masterIp, port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // sends init message (register to Master)
            Message init = new Message("Init", slaveId, null, null);
            out.writeObject(init);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while(running){

                // wait for and read exercise
                Message msg = (Message) in.readObject();
                switch(msg.type){
                    case "Exercise":
                        System.out.println("Slave " + slaveId + " hat Aufgabe erhalten, bearbeitet...");
                        ArrayList task = (ArrayList) msg.data;
                        int result = calculate((int[]) task.get(0), (int[]) task.get(1));

                        // send message
                        Message resultMsg = new Message("Result", slaveId, msg.pos, result);
                        out.writeObject(resultMsg);
                        out.flush();
                        running = false;
                        break;
                    case "Connections":
                        connections = (ArrayList) msg.data;
                        System.out.println(connections);
                        break;
                    default:
                        System.out.println("unrecognized message type");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private int calculate(int[] a, int[] b){
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        return Arrays.stream(IntStream.range(0, a.length)
                .map(i -> a[i] * b[i])
                .toArray())
                .sum();
    }
}