import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Matrix {
    private int[][] fields;

    public static boolean checkSize(Matrix x, Matrix y){
        return true;
    }

    public List<int[]> getRows(){
        return Arrays.stream(fields).toList();
    }

    public List<int[]> getCols(){
        return IntStream.range(0, fields[0].length)
                .mapToObj(col -> Arrays.stream(fields).mapToInt(field -> field[col])
                        .toArray())
                .toList();    }
}
