package com.cyber.client.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientManager {
    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 12345;

    public static String sendMessage(String message) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            System.out.println("Sending message to server: " + message);
            out.println(message);
            return in.readLine();
        } catch (Exception e) {
            System.err.println("Error sending message to server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}