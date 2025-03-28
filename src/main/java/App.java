import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
* TODO:
*  - send/receive messages
*  - timer for connections
*  - task
*  - link state/graph/bully algorithm
*/
public class App {
    public static void main(String[] args) {

        int[][] a = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        int[][] b = {
                {10, 11, 12},
                {13, 14, 15},
                {16, 17, 18}
        };

        Task m = (x, y) -> {
            Matrix matrixX = (Matrix) x;
            Matrix matrixY = (Matrix) y;
            if(Matrix.checkSize(matrixX, matrixY)) {
                List<int[][]> pairedRowsCols = matrixX.getRows()
                        .stream()
                        .flatMap(row -> matrixY.getCols()
                                .stream()
                                .map(col -> new int[][]{row, col}))
                        .toList();

                pairedRowsCols.forEach(p -> Actor.getActorsList()
                        .listIterator().next()
                    .connection.send( new Exercise(p).toString()));
            }
            return new Matrix();
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
