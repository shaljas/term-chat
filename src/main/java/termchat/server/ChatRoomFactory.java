package termchat.server;

import termchat.model.ChatRoom;
import termchat.model.MainChatRoom;
import termchat.model.User;
import termchat.repository.UserRepository;

import java.util.HashMap;
import java.util.List;

public class ChatRoomFactory {

    private final List<ChatRoom> serverChatRoomList;
    private final UserRepository users;
    private final HashMap<String, User> roomOwners = new HashMap<>();
    private final HashMap<String, ChatRoom> roomNames = new HashMap<>();
    private final ChatRoom mainChat;

    public ChatRoomFactory(List<ChatRoom> chatroomList, UserRepository users) {
        this.serverChatRoomList = chatroomList;
        this.users = users;
        this.mainChat = new MainChatRoom();
        this.serverChatRoomList.add(mainChat);
        roomNames.put("Main", mainChat);
    }

    public List<ChatRoom> getUserChatRooms(User user) {
        return serverChatRoomList
                .stream()
                .filter(c -> memberCheck(user, c))
                .toList();
    }

    public ChatRoom getMainChat() {
        return mainChat;
    }

    public boolean doesChatRoomExist(String chatname) {
        return roomNames.containsKey(chatname);
    }

    // creates a new chatroom and automatically makes creator the owner
    // return null if success, error message if not
    public String createRoom(String chatname, User user) {

        try {
            synchronized (this) {
                if (roomNames.containsKey(chatname)) {
                    return "Chatroom with that name exists already.";
                }

                int control = serverChatRoomList.size();
                ChatRoom chatroom = new ChatRoom(chatname, user);
                roomNames.put(chatname, chatroom);
                roomOwners.put(chatname, user);
                serverChatRoomList.add(chatroom);
                if (serverChatRoomList.size() == control) {
                    throw new Exception();
                }
            }
            return null;
        } catch (Exception e) {
            return "Could not create a chatroom.";
        }

    }

    public String deleteRoom(String chatname, User user) {
        ChatRoom chatroom = getRoom(chatname, user);
        if (!ownerCheck(user, chatroom)) {
            return "Only the owner can use this command.";
        }
        if (!chatroom.getMembers().isEmpty()) {
            for (User participant : chatroom.getMembers()) {
                if (participant.getActiveChat() == chatroom) {
                    participant.setActiveChat(getMainChat());
                }
            }
            chatroom.getMembers().clear();
        }
        serverChatRoomList.remove(chatroom);
        roomNames.remove(chatname);
        roomOwners.remove(chatname);
        return null;
    }

    // only return chatroom if user is in it
    public ChatRoom getRoom(String chatname, User user) {
        if (!doesChatRoomExist(chatname)) {
            return null;
        }
        ChatRoom chatroom = roomNames.get(chatname);
        if (memberCheck(user, chatroom)) {
            return chatroom;
        }
        return null;
    }

    public String addUser(User user, String username) {

        String chatname = user.getActiveChat().getName();
        ChatRoom chatroom = getRoom(chatname, user);

        if (!ownerCheck(user, chatroom)) {
            return "Only the owner can use this command.";
        }

        User userToBeAdded = users.findByUsername(username).orElse(null);

        if (userToBeAdded == null) {
            return "Could not find user " + username + ".";
        }
        if (memberCheck(userToBeAdded, chatroom)) {
            return "User is already in the chatroom.";
        }
        chatroom.addUser(userToBeAdded);
        if (chatroom.getUserByName(username) != null) {
            return null;
        }
        return "Could not add the user.";
    }

    public String removeUser(User user, String username) {

        String chatname = user.getActiveChat().getName();
        ChatRoom chatroom = getRoom(chatname, user);

        if (chatroom == null) {
            return "You are not in this chatroom.";
        }

        User userToBeRemoved = chatroom.getUserByName(username);

        if (user == userToBeRemoved) {
            return "You cannot remove yourself! Use /leave instead.";
        }

        if (!ownerCheck(user, chatroom)) {
            return "Only the owner can use this command.";
        }

        if (userToBeRemoved == null) {
            return "Could not find the user from this chatroom.";
        }

        chatroom.removeUser(userToBeRemoved);
        userToBeRemoved.setActiveChat(getMainChat());
        if (!memberCheck(userToBeRemoved, chatroom)) {
            return null;
        }
        return "Could not remove the user.";
    }

    public String leaveRoom(User user) {
        String chatname = user.getActiveChat().getName();
        ChatRoom chatroom = getRoom(chatname, user);

        if (chatroom == null) {
            return "You are not in this chatroom.";
        }

        if (chatroom.getName().equals("Main")) {
            return "You cannot leave the main chatroom.";
        }

        if (chatroom.getMembers().size() > 1 && ownerCheck(user, chatroom)) {
            return """
                    You cannot leave as the owner while there are other members in the chatroom.
                    You can:
                    A: Appoint another member as the chatroom's owner and try again.
                    B: Kick all other members first and try again.
                    C: Use /deleteroom.""";
        }
        chatroom.removeUser(user);
        user.setActiveChat(getMainChat());
        if (chatroom.getMembers().isEmpty()) {
            deleteRoom(chatname, user);
        }
        return null;
    }

    public String changeOwner(User user, String username) {

        String chatname = user.getActiveChat().getName();
        ChatRoom chatroom = getRoom(chatname, user);

        if (chatroom == null) {
            return "You are not in this chatroom.";
        }
        if (!ownerCheck(user, chatroom)) {
            return "Only the owner can use this command.";
        }

        User newOwner = chatroom.getUserByName(username);

        if (newOwner == null) {
            return "Could not find the user in this chatroom.";
        }

        roomOwners.replace(chatname, newOwner);

        if (ownerCheck(newOwner, chatroom)) {
            return null;
        }
        return "Could not change the owner.";
    }

    public String renameRoom(String newname, User user) {

        String oldname = user.getActiveChat().getName();
        ChatRoom chatroom = getRoom(oldname, user);
        if (chatroom == null) {
            return "You are not in this chatroom.";
        }
        if (!ownerCheck(user, chatroom)) {
            return "Only the owner can use this command.";
        }

        if (doesChatRoomExist(newname)) {
            return "Chatroom with that name already exists.";
        }
        try {
            chatroom.setName(newname);
            if (!chatroom.getName().equals(newname)) {
                throw new Exception();
            }
            roomNames.remove(oldname);
            roomNames.put(newname, chatroom);
            roomOwners.remove(oldname);
            roomOwners.put(newname, user);
        } catch (Exception e) {
            return "Could not change chatroom's name.";
        }
        return null;
    }

    // check if user is the current owner of chatroom
    public boolean ownerCheck(User user, ChatRoom chatroom) {
        if (chatroom.getClass() == MainChatRoom.class) return false;
        return (user == roomOwners.get(chatroom.getName()));
    }

    // check if user is in the chatroom
    public boolean memberCheck(User user, ChatRoom chatroom) {
        return (chatroom.getMembers().contains(user));
    }
}
