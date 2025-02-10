package com.cyber.client.controller;


import com.cyber.client.model.User;

public class UserDashboardController {
    private User user;

    public void setUser(User user) {
        this.user = user;
        updateUI();
    }

    private void updateUI() {
        System.out.println("User logged in: " + user.getName());
    }
}

