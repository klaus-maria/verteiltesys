import java.io.IOException;

public class App {
    public static void main(String[] args) {
        Actor master = new Actor(new Server(8080));
        Actor worker = new Actor(new Client("localhost", 8080));

        try {
            master.init();
            worker.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
