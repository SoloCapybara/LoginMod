package com.example.examplemod;

import com.example.examplemod.model.User;
import com.example.examplemod.storage.DatabaseUserStorage;
import com.example.examplemod.storage.FileUserStorage;
import com.example.examplemod.storage.UserStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import com.example.examplemod.events.LoginEventHandler;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LoginManager {
    private static LoginManager instance;
    private UserStorage storage;
    private final Map<UUID, Long> loginAttempts;
    private static final long LOGIN_TIMEOUT = 300000; // 5分钟超时
    private Map<UUID, Boolean> loggedInPlayers;

    private LoginManager() {
        initializeStorage();
        loginAttempts = new HashMap<>();
        loggedInPlayers = new HashMap<>();
    }

    private void initializeStorage() {
        if (Config.getStorageType() == Config.StorageType.FILE) {
            Path dataFile = Path.of("config", ExampleMod.MODID, "users.json");
            storage = new FileUserStorage(dataFile);
        } else {
            storage = new DatabaseUserStorage(
                Config.getDatabaseUrl(),
                Config.getDatabaseUsername(),
                Config.getDatabasePassword()
            );
        }
        storage.initialize();
    }

    public void reloadStorage() {
        if (storage != null) {
            storage.shutdown();
        }
        initializeStorage();
    }

    public static LoginManager getInstance() {
        if (instance == null) {
            instance = new LoginManager();
        }
        return instance;
    }

    public boolean register(ServerPlayer player, String password) {
        UUID uuid = player.getUUID();
        String username = player.getGameProfile().getName();

        if (storage.getUser(uuid).isPresent()) {
            player.sendSystemMessage(Component.literal("§c你已经注册过了！"));
            return false;
        }

        if (storage.getUserByUsername(username).isPresent()) {
            player.sendSystemMessage(Component.literal("§c该用户名已被使用！"));
            return false;
        }

        User user = new User(uuid, username, password);
        storage.saveUser(user);
        player.sendSystemMessage(Component.literal("§a注册成功！"));
        return true;
    }

    public boolean login(ServerPlayer player, String password) {
        UUID uuid = player.getUUID();
        Optional<User> userOpt = storage.getUser(uuid);

        if (userOpt.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c你还没有注册！"));
            return false;
        }

        User user = userOpt.get();
        if (user.isLoggedIn()) {
            player.sendSystemMessage(Component.literal("§c你已经登录了！"));
            return false;
        }

        if (!user.getPassword().equals(password)) {
            player.sendSystemMessage(Component.literal("§c密码错误！"));
            return false;
        }

        user.setLoggedIn(true);
        storage.updateUser(user);
        player.sendSystemMessage(Component.literal("§a登录成功！"));
        return true;
    }

    public void logout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Optional<User> userOpt = storage.getUser(uuid);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLoggedIn(false);
            storage.updateUser(user);
            player.sendSystemMessage(Component.literal("§a已退出登录！"));
        }
    }

    public boolean isLoggedIn(ServerPlayer player) {
        Optional<User> userOpt = storage.getUser(player.getUUID());
        return userOpt.map(User::isLoggedIn).orElse(false);
    }

    public void shutdown() {
        storage.shutdown();
    }
} 