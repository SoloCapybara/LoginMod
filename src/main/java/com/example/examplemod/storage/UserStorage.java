package com.example.examplemod.storage;

import com.example.examplemod.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserStorage {
    void saveUser(User user);
    Optional<User> getUser(UUID uuid);
    Optional<User> getUserByUsername(String username);
    void deleteUser(UUID uuid);
    void updateUser(User user);
    void initialize();
    void shutdown();
} 