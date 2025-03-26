import java.io.IOException;

/*
* TODO:
*  - send/receive messages
*  - timer for connections
*  - task
*  - link state/graph/bully algorithm
*/
public class App {
    public static void main(String[] args) {

        int[][] a = {{}};
        int[][] b = {{}};

        Task m = (int[][] x,int[][] y) -> {
            for(Actor actor: Actor.getActorsList()){

            }
            return new int[][]{};
        };
        Actor master = Actor.newMaster(8080);
        Actor worker = Actor.newWorker("localhost", 8080);

        try {
            master.init();
            worker.init();

            System.out.println(Actor.getActorsList());

            master.exercise(m, a, b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
