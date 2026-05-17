package termchat.persistence;

public class StoredMessage {
    private final int messageId;
    private final String roomName;
    private final String senderUsername;
    private final String content;
    private final String timestamp;
    private boolean delivered;

    public StoredMessage(int messageId, String roomName, String senderUsername, String content, String timestamp, boolean delivered) {
        this.messageId = messageId;
        this.roomName = roomName;
        this.senderUsername = senderUsername;
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

    public String getSenderUsername() {
        return senderUsername;
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

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}
