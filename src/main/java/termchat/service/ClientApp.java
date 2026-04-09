package termchat.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {

    private void updateThread() {

    }

    public static void main(String[] args) throws IOException {
        try (   Socket socket = new Socket("localhost",3000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner userInput = new Scanner(System.in)
        ){
            System.out.println("Client app started");

            Thread updateThread = new Thread(() -> {
                try {
                    String incoming;
                    while((incoming = in.readLine()) != null) {
                        System.out.println(incoming);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected");
                }
            });

            updateThread.start();

            while (userInput.hasNextLine()) {
                String input = userInput.nextLine();
                out.println(input);

                // TODO: ideaalis võiks olla ka kliendil teada commandid. praegu /quit sulgeb serveri pool ühenduse, aga thread jääb ikka ootama serveri outputi
                if (input.equalsIgnoreCase("/quit")) break;
            }

            updateThread.join();


        } catch (InterruptedException e) {
            // TODO: vaja paremat error handlimist, prg lic lasin IntelliJ geneda
            throw new RuntimeException(e);
        }
    }

    private static void echoTest(Scanner userInput, PrintWriter out, BufferedReader in) throws IOException {
        String incoming;
        do {
            if (userInput.hasNext()) {
                out.println(userInput.nextLine());
            }
            incoming = in.readLine();
            System.out.println(incoming);
        } while (incoming != null);
    }
}
