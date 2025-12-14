package proxy.model;

/**
 * Klasa przechowująca dane o serwerach zawartych w parametrach
 */
public class Server {
    public String protocol;  // "tcp" lub "udp"
    public String address;
    public int port;
    public String keyName;  // GET NAMES itd

    public Server(String protocol, String address, int port) {
        this.protocol = protocol;
        this.address = address;
        this.port = port;
        this.keyName = null;
    }
}
