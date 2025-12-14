package proxy.utils;

import proxy.model.Server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Klasa pomocnicza do parsowania argumentów z linii poleceń
 */
public class ArgumentParser {
    
    /**
     * Parsuje argumenty z linii poleceń
     * @param args argumenty z linii poleceń
     * @param port wrapper dla portu (będzie ustawiony)
     * @param servers lista serwerów (będzie wypełniona)
     * @return true jeśli parsowanie zakończyło się sukcesem
     */
    public static boolean parseArguments(String[] args, AtomicInteger port, List<Server> servers) {
        for (int i = 0; i < args.length; ) {
            switch (args[i]) {
                case "-port":
                    if (i + 1 >= args.length) return false;
                    try {
                        port.set(Integer.parseInt(args[i + 1]));
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    i += 2;
                    break;
                case "-servers":
                    i++;
                    while (i < args.length && !args[i].startsWith("-")) {
                        String serverStr = args[i];
                        if (!parseServer(serverStr, servers)) {
                            System.err.println("Invalid server format: " + serverStr);
                            return false;
                        }
                        i++;
                    }
                    break;
                default:
                    System.err.println("Unknown parameter: " + args[i]);
                    return false;
            }
        }

        return port.get() != 0 && !servers.isEmpty();
    }

    /**
     * Parsuje pojedynczy string serwera w formacie protocol://address:port
     */
    public static boolean parseServer(String serverStr, List<Server> servers) {
        // Format: tcp://address:port lub udp://address:port
        if (!serverStr.contains("://")) {
            return false;
        }

        String[] parts = serverStr.split("://");
        if (parts.length != 2) {
            return false;
        }

        String protocol = parts[0].toLowerCase();
        if (!protocol.equals("tcp") && !protocol.equals("udp")) {
            return false;
        }

        String[] addressPort = parts[1].split(":");
        if (addressPort.length != 2) {
            return false;
        }

        try {
            String address = addressPort[0];
            int serverPort = Integer.parseInt(addressPort[1]);
            servers.add(new Server(protocol, address, serverPort));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
