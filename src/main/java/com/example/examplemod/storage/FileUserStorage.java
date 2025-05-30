package com.example.examplemod.storage;

import com.example.examplemod.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileUserStorage implements UserStorage {
    private final Path dataFile;
    private final Gson gson;
    private Map<UUID, User> users;

    public FileUserStorage(Path dataFile) {
        this.dataFile = dataFile;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.users = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {
        try {
            if (!Files.exists(dataFile)) {
                Files.createDirectories(dataFile.getParent());
                Files.createFile(dataFile);
                saveToFile();
            } else {
                loadFromFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        saveToFile();
    }

    @Override
    public void saveUser(User user) {
        users.put(user.getUuid(), user);
        saveToFile();
    }

    @Override
    public Optional<User> getUser(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public void deleteUser(UUID uuid) {
        users.remove(uuid);
        saveToFile();
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getUuid(), user);
        saveToFile();
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(dataFile.toFile())) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        try (Reader reader = new FileReader(dataFile.toFile())) {
            TypeToken<Map<UUID, User>> typeToken = new TypeToken<Map<UUID, User>>() {};
            Map<UUID, User> loadedUsers = gson.fromJson(reader, typeToken.getType());
            if (loadedUsers != null) {
                users = new ConcurrentHashMap<>(loadedUsers);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 