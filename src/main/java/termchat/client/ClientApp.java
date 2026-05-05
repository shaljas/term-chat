package termchat.client;

import termchat.server.ServerMessageListener;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;

    public static void main(String[] args) {
        ClientApp clientApp = new ClientApp();
        clientApp.start();
    }

    public void start() {
        try (
                Socket socket = new Socket(HOST, PORT);
                DataOutputStream serverOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream serverIn = new DataInputStream(socket.getInputStream())
        ){
            System.out.println("Client app started");

            ServerMessageListener listener = startListener(serverIn, serverOut);
            handleUserInput(serverOut, listener);

        } catch (IOException e) {
            System.out.println("ClientApp start error: is the server running? " + e.getMessage());
        }
    }

    private ServerMessageListener startListener(DataInputStream serverIn, DataOutputStream serverOut) {
        ServerMessageListener listener = new ServerMessageListener(serverIn);
        listener.start();
        return listener;
    }

    private void handleUserInput(DataOutputStream serverOut, ServerMessageListener listener) throws IOException {
        Scanner userInput = new Scanner(System.in);

        while (userInput.hasNextLine()) {
            String input = userInput.nextLine();

            serverOut.writeInt(1); // message type input
            serverOut.writeUTF(input);

            if (isQuitCommand(input)) {
                listener.shutdown();
                waitForListener(listener);
                break;
            }

            String[] params = input.split(" ");
            if (params.length == 2 && isUploadCommand(params[0])) {
                ClientFileService.handleUpload(params[1], serverOut);
            }
        }
    }

    private boolean isUploadCommand(String input) {
        return "/upload".equalsIgnoreCase(input);
    }
    private boolean isQuitCommand(String input) {
        return "/quit".equalsIgnoreCase(input);
    }

    private void waitForListener(ServerMessageListener listener) {
        try {
            listener.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
