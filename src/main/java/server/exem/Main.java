package server.exem;

import javax.swing.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int PORT = 12345;
    private static final String BIND_ADDRESS = "192.168.0.115";
    public static List<ClientInfo> connectedClients = new ArrayList<>();
    public static boolean chatRoomEnabled = true;
    public static ServerGUI serverGUI;

    public static void main(String[] args) {
        // Start Swing GUI
        SwingUtilities.invokeLater(() -> {
            serverGUI = new ServerGUI(connectedClients);
            serverGUI.setVisible(true);
        });

        // Start server automatically on a separate thread
        new Thread(Main::startServer).start();
    }

    private static void startServer() {
        System.out.println("Chat server spuštěn na " + BIND_ADDRESS + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(BIND_ADDRESS))) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nový klient připojen: " + clientSocket.getInetAddress());
                ClientInfo.ClientHandler clientHandler = new ClientInfo.ClientHandler(clientSocket);
                synchronized (connectedClients) {
                    connectedClients.add(new ClientInfo(clientSocket.getInetAddress().toString(), "Neznámý", clientHandler));
                    serverGUI.updateClientList();
                    int onlineCount = connectedClients.size();
                    serverGUI.updateOnlineCount(onlineCount);
                }
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
