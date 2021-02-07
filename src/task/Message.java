package task;

import java.io.Serializable;

//responsible for the sent messages
public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    public Message(MessageType messageType) {
        this.type = messageType;
        this.data = null;
    }

    public Message(MessageType messageType, String field) {
        this.type = messageType;
        this.data = field;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
