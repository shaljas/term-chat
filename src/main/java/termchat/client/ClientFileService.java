package termchat.client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientFileService {

    public static void handleUpload(String filename, DataOutputStream out) throws IOException {

        Path filepath = Paths.get(filename);
        if (!Files.exists(filepath)) {
            System.out.println("No such file found.");
            return;
        }
        out.writeInt(2); // file type input
        out.writeUTF(filename);
        long size = Files.size(filepath);
        out.writeLong(size);

        try (FileInputStream fileIn = new FileInputStream(filepath.toFile())) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();
        }
    }

    public static String handleDownload(DataInputStream in) throws IOException {

        int result = in.readInt();
        if (result == -1) {
            return "Could not download file.";
        } else if (result == -2) {
            return "You are not in this chatroom.";
        }

        String filename = in.readUTF();
        Path target = Paths.get(filename).getFileName();

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
            return "File has been downloaded.";

        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
