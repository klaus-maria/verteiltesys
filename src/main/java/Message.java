import java.io.Serializable;

public class  Message<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    String type;
    int slaveId;
    int[] pos;
    T data;

    public Message(String type, int slaveId, int[] pos, T data) {
        this.type = type;
        this.slaveId = slaveId;
        this.pos = pos;
        this.data = data;
    }
}
