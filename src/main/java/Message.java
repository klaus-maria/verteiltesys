import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    String type;
    int slaveId;
    int row;
    int[] data;

    public Message(String type, int slaveId, int row, int[] data) {
        this.type = type;
        this.slaveId = slaveId;
        this.row = row;
        this.data = data;
    }
}
