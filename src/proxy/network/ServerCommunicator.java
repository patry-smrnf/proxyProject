package proxy.network;

import proxy.model.Server;

import java.io.*;
import java.net.*;

/**
 * Klasa odpowiedzialna za komunikację z serwerami (TCP i UDP)
 */
public class ServerCommunicator {
    
    /**
     * Wysyła komendę do serwera (TCP lub UDP) i zwraca odpowiedź
     */
    public String sendCommand(Server server, String command) throws IOException {
        if (server.protocol.equals("tcp")) {
            return sendTCPCommand(server, command);
        } else {
            return sendUDPCommand(server, command);
        }
    }

    /**
     * Wysyła komendę do serwera TCP
     */
    private String sendTCPCommand(Server server, String command) throws IOException {
        Socket socket = null;
        try {
            InetAddress address = InetAddress.getByName(server.address);
            socket = new Socket(address, server.port);
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(command);
            
            // QUIT nie zwraca odpowiedzi
            if (command.equals("QUIT")) {
                return null;
            }
            
            String response = in.readLine();
            return response;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Wysyła komendę do serwera UDP
     */
    private String sendUDPCommand(Server server, String command) throws IOException {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(server.address);
            socket = new DatagramSocket();
            
            byte[] sendData = command.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, address, server.port);
            socket.send(sendPacket);
            
            // QUIT nie zwraca odpowiedzi
            if (command.equals("QUIT")) {
                return null;
            }
            
            byte[] receiveData = new byte[256];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return response.trim();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
