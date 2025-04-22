import java.io.IOException;

public class App {
    public static void main(String[] args) {

        int[][] matrixA = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        int[][] matrixB = {
                {9, 8, 7},
                {6, 5, 4},
                {3, 2, 1}
        };

        try {
            Master.config(8080, 9, 1000, matrixA, matrixB);
            Master.spawn();

            Worker.config("127.0.0.1", 8080);
            Worker.spawn(9);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
