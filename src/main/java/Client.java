import java.net.*;
import java.io.*;
public class Client implements Connection{
    private String ip;
    private int port;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(String ip, int p){
        this.ip = ip;
        port = p;
    }

    public void start() throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void send(String msg) throws IOException {
        out.println(msg);
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
