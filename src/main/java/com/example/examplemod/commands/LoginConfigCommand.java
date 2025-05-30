package com.example.examplemod.commands;

import com.example.examplemod.Config;
import com.example.examplemod.LoginManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class LoginConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("loginconfig")
            .requires(source -> source.hasPermission(2)) // 需要OP权限
            .executes(context -> {
                sendHelp(context.getSource());
                return 1;
            })
            .then(Commands.literal("help")
                .executes(context -> {
                    sendHelp(context.getSource());
                    return 1;
                }))
            .then(Commands.literal("reload")
                .executes(context -> {
                    Config.reload();
                    LoginManager.getInstance().reloadStorage();
                    context.getSource().sendSuccess(() -> Component.literal("§a配置已重载！"), true);
                    return 1;
                }))
            .then(Commands.literal("set")
                .then(Commands.literal("storage")
                    .then(Commands.argument("type", StringArgumentType.word())
                        .executes(context -> {
                            String type = StringArgumentType.getString(context, "type");
                            try {
                                Config.StorageType storageType = Config.StorageType.valueOf(type.toUpperCase());
                                Config.setStorageType(storageType);
                                LoginManager.getInstance().reloadStorage();
                                context.getSource().sendSuccess(() -> Component.literal("§a存储方式已更改为: " + type), true);
                            } catch (IllegalArgumentException e) {
                                context.getSource().sendFailure(Component.literal("§c无效的存储类型！可用类型: FILE, DATABASE"));
                            }
                            return 1;
                        })))
                .then(Commands.literal("database")
                    .then(Commands.literal("url")
                        .then(Commands.argument("url", StringArgumentType.string())
                            .executes(context -> {
                                String url = StringArgumentType.getString(context, "url");
                                Config.setDatabaseUrl(url);
                                LoginManager.getInstance().reloadStorage();
                                context.getSource().sendSuccess(() -> Component.literal("§a数据库URL已更新！"), true);
                                return 1;
                            })))
                    .then(Commands.literal("username")
                        .then(Commands.argument("username", StringArgumentType.string())
                            .executes(context -> {
                                String username = StringArgumentType.getString(context, "username");
                                Config.setDatabaseUsername(username);
                                LoginManager.getInstance().reloadStorage();
                                context.getSource().sendSuccess(() -> Component.literal("§a数据库用户名已更新！"), true);
                                return 1;
                            })))
                    .then(Commands.literal("password")
                        .then(Commands.argument("password", StringArgumentType.string())
                            .executes(context -> {
                                String password = StringArgumentType.getString(context, "password");
                                Config.setDatabasePassword(password);
                                LoginManager.getInstance().reloadStorage();
                                context.getSource().sendSuccess(() -> Component.literal("§a数据库密码已更新！"), true);
                                return 1;
                            })))))
            .then(Commands.literal("get")
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§6当前配置：\n" +
                            "§e存储方式: §f" + Config.getStorageType() + "\n" +
                            "§e数据库URL: §f" + Config.getDatabaseUrl() + "\n" +
                            "§e数据库用户名: §f" + Config.getDatabaseUsername() + "\n" +
                            "§e数据库密码: §f" + (Config.getDatabasePassword().isEmpty() ? "未设置" : "已设置")), false);
                    return 1;
                })));
    }

    private static void sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6===== 登录模组配置命令帮助 =====\n" +
                "§e/loginconfig help §f- 显示此帮助信息\n" +
                "§e/loginconfig get §f- 查看当前配置\n" +
                "§e/loginconfig reload §f- 重载配置文件\n" +
                "§e/loginconfig set storage <FILE|DATABASE> §f- 设置存储方式 (强烈不建议在生产环境中使用文件存储，仅推荐测试时使用)\n" +
                "§e/loginconfig set database url <url> §f- 设置数据库URL\n" +
                "§e/loginconfig set database username <username> §f- 设置数据库用户名\n" +
                "§e/loginconfig set database password <password> §f- 设置数据库密码\n" +
                "§6================================\n" +
                "§c注意：修改存储方式或数据库连接信息后，强烈建议重启服务器以确保配置完全生效！"), false);
    }
} 