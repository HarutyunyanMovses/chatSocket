package ServerSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private static Set<String> clientNames = new HashSet<>();
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Request and read the client's name
            out.println("Please enter your name:");
            String requestedName;
            while (true) {
                requestedName = in.readLine();
                synchronized (clientNames) {
                    if (requestedName != null && !requestedName.trim().isEmpty() && !clientNames.contains(requestedName)) {
                        clientNames.add(requestedName);
                        clientName = requestedName;
                        break;
                    } else {
                        out.println("Name is already taken or invalid. Please enter a different name:");
                    }
                }
            }

            synchronized (clientHandlers) {
                clientHandlers.add(this);
            }

            sendMessageToAll(clientName + " has joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                sendMessageToAll(clientName + ": " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }
                synchronized (clientNames) {
                    clientNames.remove(clientName);
                }
                sendMessageToAll(clientName + " has left the chat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageToAll(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.out.println(message);
            }
        }
    }
}
