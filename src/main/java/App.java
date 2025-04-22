import java.io.IOException;

public class App {
    public static void main(String[] args) {

        try {
            Master m = new Master();
            m.start(8080, 9, 1000);

            Worker.config("127.0.0.1", 8080);
            Worker.spawn(9);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
