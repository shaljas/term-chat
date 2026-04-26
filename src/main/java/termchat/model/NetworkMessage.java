package termchat.model;

public class NetworkMessage {
    private int messageId;
    private String senderUsername;
    private String content;
    private String timestamp;
    private String roomId;

    // JSON parseri jaoks
    public NetworkMessage() {
    }

    public NetworkMessage(int messageId, String senderUsername, String content, String timestamp, String roomId) {
        this.messageId = messageId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.timestamp = timestamp;
        this.roomId = roomId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
