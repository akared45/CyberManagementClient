package com.cyber.client.client;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientManager {
    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 12345;

    public static void sendStatus(String status) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
            System.out.println("Sending status to server: " + status);
            out.println(status);
        } catch (Exception e) {
            System.err.println("Error sending status to server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}