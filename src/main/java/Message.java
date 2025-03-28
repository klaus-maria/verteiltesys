import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    String type;
    int slaveId;
    byte[] data;

    public Message(String type, int slaveId, byte[] data) {
        this.type = type;
        this.slaveId = slaveId;
        this.data = data;
    }
}
