package com.example.examplemod.model;

import java.util.UUID;

public class User {
    private final UUID uuid;
    private String username;
    private String password;
    private boolean isLoggedIn;

    public User(UUID uuid, String username, String password) {
        this.uuid = uuid;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
} 