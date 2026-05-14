package termchat.model;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import static termchat.model.Ansi.GRAY;
import static termchat.model.Ansi.RESET;

public class Message {

    private final int messageId;
    private final String content;
    private final User sender;
    private final LocalDateTime timestamp;
    private boolean delivered;

    public Message(int messageId, String content, User sender, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public void markAsDelivered() {
        this.delivered = true;
    }

    public String format() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return GRAY + "[" + timestamp.format(formatter) +"] " + sender.getUsername() + ": " + content + RESET;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isDelivered() {
        return delivered;
    }

    @Override
    public String toString() {
        return format();
    }

}
