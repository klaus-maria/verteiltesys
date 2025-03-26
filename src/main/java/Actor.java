import java.io.IOException;

public class Actor {
    private Connection connection;

    public Actor(Connection c){
        connection = c;
    }

    public void init() throws IOException {
        connection.start();
    }

    public void close() throws IOException {
        connection.stop();
    }
}
