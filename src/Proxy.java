import proxy.core.ProxyObject;
import proxy.utils.ArgumentParser;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Główna klasa aplikacji proxy
 */
public class Proxy extends ProxyObject {

    public static void main(String[] args) {
        Proxy proxy = new Proxy();
        if (!proxy.parseArguments(args)) {
            System.err.println("Incorrect execution syntax");
            System.err.println("Usage: java Proxy -port <port> -servers <server1> [server2] ...");
            System.err.println("Example: java Proxy -port 8080 -servers tcp://localhost:1234 udp://localhost:5678");
            System.exit(1);
        }
        proxy.start();
    }

    private boolean parseArguments(String[] args) {
        servers = new ArrayList<>();
        AtomicInteger portWrapper = new AtomicInteger(0);
        
        boolean result = ArgumentParser.parseArguments(args, portWrapper, servers);
        
        if (result) {
            this.port = portWrapper.get();
        }
        
        return result;
    }
}