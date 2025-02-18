package com.cyber.client.client;
public class ClientStatus {
    public static void sendOnlineStatus() {
        ClientManager.sendMessage("ONLINE");
    }

    public static void sendOfflineStatus() {
        ClientManager.sendMessage("OFFLINE");
    }

}