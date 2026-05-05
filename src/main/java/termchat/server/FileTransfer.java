package termchat.server;

import termchat.client.ClientHandler;
import termchat.model.ChatRoom;
import termchat.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTransfer {

    private final Path fileStorage;
    private final Server server;

    // response codes
    static final int OK = 0;
    static final int ERROR = -1;
    static final int AUTH_DENIED = -2;

    public FileTransfer(Server server, String fileStorage) {
        this.server = server;
        this.fileStorage = Paths.get(fileStorage);

    }

    public void receiveFile(ChatRoom room, User user) throws IOException {

        ClientHandler client = user.getClientHandler();

        if (!server.RoomManager().memberCheck(user, room)) {
            client.sendToClient("You are not in this chatroom."); // denied
            return;
        }
        DataInputStream in = client.getIn();
        String filename = in.readUTF();

        // for sanitization purposes
        filename = Paths.get(filename).getFileName().toString();
        Path subdir = fileStorage.resolve(room.getName());
        Files.createDirectories(subdir);
        Path target = subdir.resolve(filename);

        try (FileOutputStream fos = new FileOutputStream(target.toFile())) {

            byte[] buffer = new byte[8192];
            long remaining = in.readLong();
            while (remaining > 0) {
                int bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));

                if (bytesRead == -1) {
                    throw new IOException("Connection lost during file transfer.");
                }

                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            client.sendToClient("File uploaded successfully."); // success

        } catch (IOException e) {
            client.sendToClient("An error has occurred."); // error
        }
    }

    public void sendFile(ChatRoom room, User user, String filename) throws IOException {

        DataOutputStream out = user.getClientHandler().getOut();
        out.writeInt(2); // file type input
        if (!server.RoomManager().memberCheck(user, room)) {
            out.writeInt(AUTH_DENIED);
            return;
        }

        filename = Paths.get(filename).getFileName().toString();
        Path file = fileStorage.resolve(room.getName()).resolve(filename);

        if (!Files.exists(file)) {
            out.writeInt(ERROR);
            return;
        }

        out.writeInt(OK);
        out.writeUTF(filename);
        out.writeLong(Files.size(file));
        out.flush();

        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            out.writeInt(ERROR);
            throw e;
        }
    }
}
