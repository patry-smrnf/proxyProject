package proxy.handlers;

import proxy.core.ProxyObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Klasa obsługująca pojedyncze połączenie klienta
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ProxyObject proxy;

    public ClientHandler(Socket clientSocket, ProxyObject proxy) {
        this.clientSocket = clientSocket;
        this.proxy = proxy;
    }

    @Override
    public void run() {
        try {
            Scanner in = new Scanner(clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String command = in.nextLine().trim();
            System.out.println("Received command from client: " + command);

            String response = proxy.processCommand(command);
            
            if (response != null) {
                System.out.println("Sending response to client: " + response);
                out.println(response);
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e);
        }
    }
}
