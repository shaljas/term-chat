package termchat.persistence;

public class StoredMessage {
    private int messageId;
    private String roomName;
    private String content;
    private String timestamp;
    private boolean delivered;

    public StoredMessage() {
    }

    public StoredMessage(int messageId, String roomName, String content, String timestamp, boolean delivered) {
        this.messageId = messageId;
        this.roomName = roomName;
        this.content = content;
        this.timestamp = timestamp;
        this.delivered = delivered;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isDelivered() {
        return delivered;
    }
}
