import java.io.IOException;
import java.util.Optional;

public interface Connection {
    void start() throws IOException;
    void stop() throws IOException;
}
