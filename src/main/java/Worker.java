import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.stream.IntStream;

class Worker {
    private int slaveId;

    public void start(String masterIp, int port) {

        try (Socket socket = new Socket(masterIp, port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message initMsg = (Message) in.readObject();
            if ("Initialize".equals(initMsg.type)) {
                slaveId = initMsg.slaveId;
                System.out.println("Slave " + slaveId + " verbunden mit Master");
            }

            Message msg = (Message) in.readObject();
            if ("Exercise".equals(msg.type)) {
                System.out.println("Slave " + slaveId + " hat Aufgabe erhalten, bearbeitet...");
                int[] resultRow = multiplyRow(msg.data, new int[][] {
                        {9, 8, 7},
                        {6, 5, 4},
                        {3, 2, 1}
                });

                Message resultMsg = new Message("Result", slaveId, msg.row, resultRow);
                out.writeObject(resultMsg);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private int[] multiplyRow(int[] rowData, int[][] matrixB) {
        return IntStream.range(0, matrixB[0].length)
                .map(col -> IntStream.range(0, rowData.length)
                        .map(i -> rowData[i] * matrixB[i][col])
                        .sum())
                .toArray();
    }
}