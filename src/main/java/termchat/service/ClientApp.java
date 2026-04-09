package termchat.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) throws IOException {
        try (   Socket socket = new Socket("localhost",3000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner userInput = new Scanner(System.in)
        ){
            System.out.println("Client app started");

            InputListenerService listener = new InputListenerService(in);

            listener.start();

            while (userInput.hasNextLine()) {
                String input = userInput.nextLine();
                out.println(input);

                // TODO: ideaalis võiks olla ka kliendil teada commandid. praegu /quit sulgeb serveri pool ühenduse, aga loop jääb ikka ootama serveri outputi
                if (input.equalsIgnoreCase("/quit")) break;
            }

            listener.waitForCompleation();


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
