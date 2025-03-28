import java.net.*;
import java.io.*;
import java.util.Random;

public class Client {
    private static final String MASTER_IP = "127.0.0.1";
    private static final int PORT = 9120;
    private static final int SLAVE_ID = new Random().nextInt(1000);

    public static void start() {
        try (Socket socket = new Socket(MASTER_IP, PORT)) {
            System.out.println("Slave " + SLAVE_ID + " verbunden mit Master");
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message initMsg = new Message("Initialize", SLAVE_ID, new byte[0]);
            out.writeObject(initMsg);
            out.flush();

            Message msg = (Message) in.readObject();
            if ("Exercise".equals(msg.type)) {
                System.out.println("Slave " + SLAVE_ID + " hat Aufgabe erhalten, bearbeitet...");
                Thread.sleep(1000);

                Message resultMsg = new Message("Result", SLAVE_ID, msg.data);
                out.writeObject(resultMsg);
                out.flush();
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
