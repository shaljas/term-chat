package termchat.server;

import termchat.client.ClientHandler;
import termchat.repository.MessageRepository;
import termchat.repository.UserRepository;
import termchat.service.AuthService;
import termchat.service.HistoryLoaderService;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 3000;
    private static final int THREAD_POOL_SIZE = 6;
    private static final String FILE_STORAGE_PATH = "Data/files";

    private final SessionManager sessionManager;
    private final AuthService authService;
    private final MessageRouter messageRouter;
    private final ChatRoomFactory chatRoomFactory;
    private final FileTransfer fileTransfer;

    public Server() {
        this.sessionManager = new SessionManager();
        this.authService = new AuthService(new UserRepository());
        this.chatRoomFactory = new ChatRoomFactory(authService.getUserRepository());
        this.messageRouter = new MessageRouter(new MessageRepository(), authService.getUserRepository(), sessionManager);
        this.fileTransfer = new FileTransfer(FILE_STORAGE_PATH, chatRoomFactory);

        new HistoryLoaderService(new MessageRepository(), chatRoomFactory, authService).loadChatHistoryFromStorage();
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server is now listening.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(
                        new ClientHandler(
                                clientSocket,
                                sessionManager,
                                fileTransfer,
                                authService,
                                chatRoomFactory,
                                messageRouter
                ));
            }
        } finally {
            pool.shutdown();
        }
    }
}
