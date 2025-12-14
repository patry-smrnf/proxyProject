package proxy.core;

import proxy.model.Server;
import proxy.network.ServerCommunicator;
import proxy.commands.CommandProcessor;
import proxy.discovery.ServerDiscovery;
import proxy.discovery.ServerFinder;
import proxy.handlers.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Główna klasa bazowa proxy - zarządza całą logiką serwera proxy
 */
public abstract class ProxyObject {
    protected int port;
    protected List<Server> servers;
    protected ServerSocket tcpServerSocket;
    protected ExecutorService executorService;
    protected volatile boolean running = true;
    
    protected ServerCommunicator serverCommunicator;
    protected CommandProcessor commandProcessor;
    protected ServerDiscovery serverDiscovery;

    /**
     * Uruchamia serwer proxy
     */
    public void start() {
        try {
            // Inicjalizacja komponentów
            initializeComponents();
            
            System.out.println("Starting proxy on port " + port);
            tcpServerSocket = new ServerSocket(port);
            System.out.println("Proxy socket created");
            
            // Inicjalizacja puli wątków do obsługi klientów
            executorService = Executors.newCachedThreadPool();
            
            // Wstępne pobranie informacji o kluczach z serwerów
            serverDiscovery.discoverKeys(servers);
            
            System.out.println("Proxy ready. Waiting for clients...");
            
            // Główna pętla nasłuchiwania
            while (running) {
                try {
                    Socket clientSocket = tcpServerSocket.accept();
                    System.out.println("Client connected from " + 
                        clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());
                    
                    executorService.submit(new ClientHandler(clientSocket, this));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't create proxy socket: " + e);
            System.exit(1);
        }
    }

    /**
     * Inicjalizuje komponenty proxy
     */
    protected void initializeComponents() {
        serverCommunicator = new ServerCommunicator();
        ServerFinder serverFinder = new ServerFinder();
        commandProcessor = new CommandProcessor(servers, serverCommunicator, serverFinder);
        serverDiscovery = new ServerDiscovery(serverCommunicator);
    }

    /**
     * Przetwarza komendę od klienta
     */
    public String processCommand(String command) {
        String response = commandProcessor.processCommand(command);
        
        // Jeśli otrzymano QUIT (response == null), zamknij proxy
        if (response == null && command != null && command.trim().startsWith("QUIT")) {
            shutdown();
        }
        
        return response;
    }

    /**
     * Zamyka proxy i wszystkie zasoby
     */
    protected void shutdown() {
        running = false;
        
        // Zamknięcie socketów
        try {
            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e);
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        System.exit(0);
    }
}
