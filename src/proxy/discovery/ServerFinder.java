package proxy.discovery;

import proxy.model.Server;

import java.util.List;

/**
 * Klasa odpowiedzialna za znajdowanie serwerów przechowujących określone klucze
 */
public class ServerFinder {
    
    /**
     * Znajduje serwer, który przechowuje dany klucz
     */
    public Server findServerForKey(List<Server> servers, String keyName) {
        for (Server server : servers) {
            if (keyName.equals(server.keyName)) {
                return server;
            }
        }
        return null;
    }
}
