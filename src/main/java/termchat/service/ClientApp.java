package termchat.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ){
            System.out.println("Client app started");

            ServerMessageListener listener = startListener(serverIn);
            handleUserInput(serverOut, listener);

        } catch (IOException e) {
            System.out.println("ClientApp start error: is the server running? " + e.getMessage());
        }
    }

    private ServerMessageListener startListener(BufferedReader serverIn) {
        ServerMessageListener listener = new ServerMessageListener(serverIn);
        listener.start();
        return listener;
    }

    private void handleUserInput(PrintWriter serverOut, ServerMessageListener listener) {
        Scanner userInput = new Scanner(System.in);

        while (userInput.hasNextLine()) {
            String input = userInput.nextLine();

            serverOut.println(input);

            if (isQuitCommand(input)) {
                listener.shutdown();
                waitForListener(listener);
                break;
            }
        }
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
