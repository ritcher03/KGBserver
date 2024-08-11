package server.exem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientInfo {
    private String ipAddress;
    private String name;
    private ClientHandler handler;

    public ClientInfo(String ipAddress, String name, ClientHandler handler) {
        this.ipAddress = ipAddress;
        this.name = name;
        this.handler = handler;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() {
        return name;
    }


    public ClientHandler getHandler() {
        return handler;
    }

    public Socket getClientSocket() {
        return handler.getClientSocket(); // Return the socket from the handler
    }

    public static class ClientHandler extends Thread {
        private Socket clientSocket;
        PrintWriter out;
        private BufferedReader in;
        private String currentRoom = "Global1"; // Default room

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Request client name and password
                out.println("Jméno:");
                String clientName = in.readLine();
                out.println("Heslo:");
                String password = in.readLine();

                // Authenticate user
                boolean isAuthenticated = SQLSecurity.authenticateUser(clientName, password);

                if (!isAuthenticated) {
                    out.println("Přihlášení selhalo!");
                    clientSocket.close();
                    return;
                }
                out.println("Přihlášení úspěšné!");

                synchronized (Main.connectedClients) {
                    ClientInfo clientInfo = new ClientInfo(clientSocket.getInetAddress().toString(), clientName, this);
                    Main.connectedClients.add(clientInfo);
                    Main.serverGUI.updateClientList();
                    Main.serverGUI.updateOnlineCount(Main.connectedClients.size());

                    // Notify the new client about the online count
                    out.println("/onlineCount " + Main.connectedClients.size());
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/room ")) {
                        currentRoom = message.substring(6); // Update room
                        continue;
                    }

                    String formattedMessage = clientName + " (" + currentRoom + "): " + message;
                    System.out.println(formattedMessage);

                    if (Main.serverGUI != null) {
                        Main.serverGUI.appendToConsole(formattedMessage);
                    }

                    if (Main.chatRoomEnabled) {
                        broadcastMessage(formattedMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                disconnectClient();
            }
        }

        private void broadcastMessage(String message) {
            synchronized (Main.connectedClients) {
                for (ClientInfo client : Main.connectedClients) {
                    if (client.getHandler().currentRoom.equals(this.currentRoom)) {
                        client.getHandler().out.println(message);
                    }
                }
                // Notify clients of the updated online count
                Main.serverGUI.updateOnlineCount(Main.connectedClients.size());
            }
        }

        private void disconnectClient() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (Main.connectedClients) {
                Main.connectedClients.removeIf(client -> client.getHandler() == this);
                if (Main.serverGUI != null) {
                    Main.serverGUI.appendToConsole("Klient odpojen: " + clientSocket.getInetAddress());
                    Main.serverGUI.updateClientList();
                    Main.serverGUI.updateOnlineCount(Main.connectedClients.size()); // Update online count
                }
            }
            System.out.println("Klient odpojen: " + clientSocket.getInetAddress());
        }

        public void setCurrentRoom(String room) {
            this.currentRoom = room;
        }
    }
}
