package server.exem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ServerGUI extends JFrame {
    private JTextArea consoleArea;
    private JButton exitButton;
    private JLabel dbStatusLabel;
    private JLabel onlineCountLabel;
    private JList<String> clientList;
    private DefaultListModel<String> listModel;
    private JComboBox<String> roomComboBox;
    private DefaultComboBoxModel<String> roomModel;

    public ServerGUI(List<ClientInfo> connectedClients) {
        setTitle("Serverová konzole");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        add(scrollPane, BorderLayout.CENTER);

        dbStatusLabel = new JLabel("Stav databáze: Neznámý");
        onlineCountLabel = new JLabel("Online uživatelé: 0");
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.add(dbStatusLabel);
        statusPanel.add(onlineCountLabel);
        add(statusPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        add(clientScrollPane, BorderLayout.EAST);

        roomModel = new DefaultComboBoxModel<>(new String[]{"Global1", "Global2", "Global3"});
        roomComboBox = new JComboBox<>(roomModel);

        JPanel roomPanel = new JPanel();
        roomPanel.add(new JLabel("Místnost:"));
        roomPanel.add(roomComboBox);
        add(roomPanel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel();
        add(controlPanel, BorderLayout.SOUTH);

        exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            System.out.println("Server se vypne...");
            System.exit(0);
        });
        controlPanel.add(exitButton);

        updateDatabaseStatus();
    }

    public void appendToConsole(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(message + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public void updateDatabaseStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean isDatabaseConnected = SQLSecurity.isDatabaseConnected();
            dbStatusLabel.setText("Stav databáze: " + (isDatabaseConnected ? "Připojeno" : "Nepřipojeno"));
        });
    }

    public void updateClientList() {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            synchronized (Main.connectedClients) {
                for (ClientInfo client : Main.connectedClients) {
                    listModel.addElement(client.getName() + " (" + client.getIpAddress() + ")");
                }
            }
        });
    }

    public void updateOnlineCount(int count) {
        SwingUtilities.invokeLater(() -> {
            onlineCountLabel.setText("Online uživatelé: " + count);
        });
    }

    // Method to handle server responses
    public void serverResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            if (response.startsWith("/onlineCount ")) {
                int count = Integer.parseInt(response.substring(13));
                updateOnlineCount(count);
            } else {
                appendToConsole(response);
            }
        });
    }
}
