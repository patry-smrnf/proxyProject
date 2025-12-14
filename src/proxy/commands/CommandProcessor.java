package proxy.commands;

import proxy.model.Server;
import proxy.network.ServerCommunicator;
import proxy.discovery.ServerFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa odpowiedzialna za przetwarzanie i obsługę komend od klientów
 */
public class CommandProcessor {
    private List<Server> servers;
    private ServerCommunicator communicator;
    private ServerFinder serverFinder;
    
    public CommandProcessor(List<Server> servers, ServerCommunicator communicator, ServerFinder serverFinder) {
        this.servers = servers;
        this.communicator = communicator;
        this.serverFinder = serverFinder;
    }
    
    /**
     * Przetwarza komendę od klienta i zwraca odpowiedź
     */
    public String processCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "NA";
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 0) {
            return "NA";
        }

        String cmd = parts[0];

        switch (cmd) {
            case "GET":
                if (parts.length < 2) {
                    return "NA";
                }
                if (parts[1].equals("NAMES")) {
                    return handleGetNames();
                } else if (parts[1].equals("VALUE")) {
                    if (parts.length < 3) {
                        return "NA";
                    }
                    return handleGetValue(parts[2]);
                } else {
                    return "NA";
                }

            case "SET":
                if (parts.length < 3) {
                    return "NA";
                }
                try {
                    String keyName = parts[1];
                    int value = Integer.parseInt(parts[2]);
                    return handleSet(keyName, value);
                } catch (NumberFormatException e) {
                    return "NA";
                }

            case "QUIT":
                return handleQuit();

            default:
                return "NA";
        }
    }

    /**
     * Obsługa GET NAMES - zwraca wszystkie klucze z serwerów
     */
    private String handleGetNames() {
        List<String> allKeys = new ArrayList<>();
        
        for (Server server : servers) {
            try {
                String response = communicator.sendCommand(server, "GET NAMES");
                if (response != null && response.startsWith("OK")) {
                    String[] parts = response.split("\\s+");
                    if (parts.length >= 3) {
                        // parts[1] to liczba, parts[2+] to nazwy kluczy
                        for (int i = 2; i < parts.length; i++) {
                            if (!allKeys.contains(parts[i])) {
                                allKeys.add(parts[i]);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting names from server: " + e);
            }
        }
        
        if (allKeys.isEmpty()) {
            return "OK 0";
        }
        
        StringBuilder result = new StringBuilder("OK " + allKeys.size());
        for (String key : allKeys) {
            result.append(" ").append(key);
        }
        return result.toString();
    }

    /**
     * Obsługa GET VALUE - przekazuje do odpowiedniego serwera
     */
    private String handleGetValue(String keyName) {
        Server server = serverFinder.findServerForKey(servers, keyName);
        if (server == null) {
            return "NA";
        }
        
        try {
            String response = communicator.sendCommand(server, "GET VALUE " + keyName);
            return response != null ? response : "NA";
        } catch (Exception e) {
            System.err.println("Error getting value from server: " + e);
            return "NA";
        }
    }

    /**
     * Obsługa SET - przekazuje do odpowiedniego serwera
     */
    private String handleSet(String keyName, int value) {
        Server server = serverFinder.findServerForKey(servers, keyName);
        if (server == null) {
            return "NA";
        }
        
        try {
            String response = communicator.sendCommand(server, "SET " + keyName + " " + value);
            return response != null ? response : "NA";
        } catch (Exception e) {
            System.err.println("Error setting value on server: " + e);
            return "NA";
        }
    }

    /**
     * Obsługa QUIT - przekazuje do wszystkich serwerów
     * Zwraca null, aby sygnalizować, że proxy powinno się zamknąć
     */
    private String handleQuit() {
        System.out.println("QUIT command received. Shutting down...");
        
        // Przekazanie QUIT do wszystkich serwerów
        for (Server server : servers) {
            try {
                communicator.sendCommand(server, "QUIT");
            } catch (Exception e) {
                System.err.println("Error sending QUIT to server: " + e);
            }
        }
        
        return null; // QUIT nie zwraca odpowiedzi
    }
}
