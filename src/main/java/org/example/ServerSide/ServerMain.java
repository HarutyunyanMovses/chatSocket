package org.example.ServerSide;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class ServerMain {
        private static final int PORT = 3030;

        public static void main(String[] args) {
            System.out.println("Chat server started...");
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientHandler(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

}


