import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Actor {
    private static List<Actor> actorsList = new ArrayList<>();
    private Connection connection;

    private Actor(Connection c){
        connection = c;
    }

    public static Actor newMaster(int port){
        Actor a = new Actor(new Server(port));
        actorsList.add(a);
        return a;
    }

    public static Actor newWorker(String ip, int port){
        Actor a = new Actor(new Client(ip, port));
        actorsList.add(a);
        return a;
    }

    public void init() throws IOException {
        connection.start();
    }

    public void close() throws IOException {
        connection.stop();
    }

    public static List<Actor> getActorsList() {
        return actorsList;
    }
}
