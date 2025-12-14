package proxy.discovery;

import proxy.model.Server;
import proxy.network.ServerCommunicator;

import java.util.List;

/**
 * Klasa odpowiedzialna za odkrywanie kluczy dostępnych na serwerach
 */
public class ServerDiscovery {
    private ServerCommunicator communicator;
    
    public ServerDiscovery(ServerCommunicator communicator) {
        this.communicator = communicator;
    }
    
    /**
     * Odkrywa klucze dostępne na wszystkich serwerach
     */
    public void discoverKeys(List<Server> servers) {
        System.out.println("Discovering keys from servers...");
        for (Server server : servers) {
            try {
                String response = communicator.sendCommand(server, "GET NAMES");
                if (response != null && response.startsWith("OK")) {
                    String[] parts = response.split("\\s+");
                    if (parts.length >= 3) {
                        server.keyName = parts[2]; // Pierwszy klucz (dla prostoty zakładamy jeden klucz na serwer)
                        System.out.println("Server " + server.protocol + "://" + server.address + 
                            ":" + server.port + " has key: " + server.keyName);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error discovering key from server " + 
                    server.protocol + "://" + server.address + ":" + server.port + ": " + e);
            }
        }
    }
}
