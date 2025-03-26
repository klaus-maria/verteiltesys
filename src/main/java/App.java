import java.io.IOException;

/*
* TODO:
*  - send/recieve messages
*  - timer for connections
*  - task
*  - link state/graph/bully algorithm
*/
public class App {
    public static void main(String[] args) {
        Actor master = Actor.newMaster(8080);
        Actor worker = Actor.newWorker("localhost", 8080);

        try {
            master.init();
            worker.init();
            System.out.println(Actor.getActorsList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
