import java.util.Optional;

public interface Task<T> {
    T perform(T a, T b);
}
