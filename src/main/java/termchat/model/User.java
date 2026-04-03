package termchat.model;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private boolean isOnline;

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isOnline = false;
    }

    public String getUsername() {
        return username;
    }

    void login() {
        this.isOnline = true;
        // TODO: implementeerida login loogika, nt kontrollida parooli ja uuendada kasutaja olekut
    }

    void logout() {
        this.isOnline = false;
        // TODO: implementeerida logout loogika, nt uuendada kasutaja olekut, sessioonist välja logimine jne
    }

    void sendMessage(String message, User recipient) {
        // TODO: implementeerida sõnumi saatmise loogika, nt luua sõnumi objekt, salvestada see andmebaasi ja saata see Userile
        // v chat roomi?
    }

    void joinChatRoom(ChatRoom chatRoom) {
        // TODO: implementeerida chat roomi liitumise loogika, nt lisada kasutaja chat roomi osalejate nimekirja
    }

    void leaveChatRoom(ChatRoom chatRoom) {
        // samamoodi võiks olla ka loogika chatRoomist lahkumiseks, nt eemaldada kasutaja chat roomi osalejate nimekirjast
    }

    List<Message> getMessageHistory(ChatRoom chatRoom) {
        return null; // TODO: implementeerida sõnumi ajaloo toomise loogika, nt pärida sõnumid andmebaasist ja tagastada need
    }
}
