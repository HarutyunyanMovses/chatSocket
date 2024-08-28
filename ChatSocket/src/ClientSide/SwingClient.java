package ClientSide;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SwingClient {
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private JFrame frame;
    private JTextField textField;
    private JTextPane textPane;
    private JButton sendButton;

    private StyledDocument doc;
    private Style clientStyle;
    private Style otherClientStyle;

    private String clientName;

    public SwingClient(String serverAddress, int port) {
        initializeConnection(serverAddress, port);
        initializeGUI();
        startServerListener();
    }

    private void initializeConnection(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Request the client's name
            String response = input.readLine();
            if (response != null && response.equals("Please enter your name:")) {
                clientName = JOptionPane.showInputDialog(frame, "Enter your name:", "Client Name", JOptionPane.PLAIN_MESSAGE);
                if (clientName == null || clientName.trim().isEmpty()) {
                    clientName = "Anonymous";
                }
                output.println(clientName);
            }
        } catch (IOException e) {
            showErrorDialog("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Swing Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setEditable(false);
        doc = textPane.getStyledDocument();

        // Define styles for client and other clients
        clientStyle = doc.addStyle("ClientStyle", null);
        StyleConstants.setForeground(clientStyle, Color.BLUE);
        StyleConstants.setFontSize(clientStyle, 18);

        otherClientStyle = doc.addStyle("OtherClientStyle", null);
        StyleConstants.setForeground(otherClientStyle, Color.BLACK);
        StyleConstants.setFontSize(otherClientStyle, 18);

        frame.add(new JScrollPane(textPane), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField();
        panel.add(textField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);
        frame.add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        textField.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    private void startServerListener() {
        new Thread(() -> {
            try {
                String response;
                while ((response = input.readLine()) != null) {
                    if (response.startsWith(clientName + ": ")) {
                        continue;
                    }
                    appendMessage(response, otherClientStyle);
                }
            } catch (IOException e) {
                showErrorDialog("Error reading from server: " + e.getMessage());
                e.printStackTrace();
            } finally {
                closeResources();
            }
        }).start();
    }

    private void sendMessage() {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            String formattedMessage = clientName + ": " + message;
            appendMessage(formattedMessage, clientStyle); // Display own message
            output.println(formattedMessage); // Send formatted message
            textField.setText("");
        }
    }

    private void appendMessage(String message, Style style) {
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void closeResources() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
