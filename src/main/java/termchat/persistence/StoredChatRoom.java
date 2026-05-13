package termchat.persistence;

import java.util.ArrayList;
import java.util.List;

public class StoredChatRoom {
    private String id;
    private String name;
    private String ownerUsername;
    private List<String> memberUsernames;

    public StoredChatRoom() {
        this.memberUsernames = new ArrayList<>();
    }

    public StoredChatRoom(String id, String name, String ownerUsername, List<String> memberUsernames) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.memberUsernames = memberUsernames;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public List<String> getMemberUsernames() {
        return memberUsernames;
    }
}
