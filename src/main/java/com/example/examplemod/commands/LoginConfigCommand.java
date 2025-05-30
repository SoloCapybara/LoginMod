package com.example.examplemod.commands;

import com.example.examplemod.Config;
import com.example.examplemod.LoginManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
            .then(Commands.literal("get") // 查看当前配置
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§6===== 当前登录模组配置 =====\n" +
                            "§e存储方式: §f" + Config.getStorageType() + "\n" +
                            "§e数据库URL: §f" + Config.getDatabaseUrl() + "\n" +
                            "§e数据库用户名: §f" + Config.getDatabaseUsername() + "\n" +
                            "§e数据库密码: §f" + (Config.getDatabasePassword().isEmpty() ? "未设置" : "已设置") + "\n" +
                            "§e禁止未登录玩家移动: §f" + Config.shouldRestrictMovement() + "\n" + // 显示新配置
                            "§e对未登录玩家施加失明效果: §f" + Config.shouldApplyBlindness()), false); // 显示新配置
                    return 1;
                }))
            .then(Commands.literal("set") // 设置配置
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
                            }))))
                .then(Commands.literal("restrictMovement") // 新增设置移动限制的子命令
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            Config.setRestrictMovement(value);
                            context.getSource().sendSuccess(() -> Component.literal("§a禁止未登录玩家移动设置已更新为: " + value), true);
                            return 1;
                        })))
                .then(Commands.literal("applyBlindness") // 新增设置失明效果的子命令
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            Config.setApplyBlindness(value);
                            context.getSource().sendSuccess(() -> Component.literal("§a对未登录玩家施加失明效果设置已更新为: " + value), true);
                            return 1;
                        }))))
        );
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
                "§e/loginconfig set restrictMovement <true|false> §f- 设置是否禁止未登录玩家移动\n" + // 更新帮助信息
                "§e/loginconfig set applyBlindness <true|false> §f- 设置是否对未登录玩家施加失明效果\n" + // 更新帮助信息
                "§6================================\n" +
                "§c注意：修改存储方式或数据库连接信息后，强烈建议重启服务器以确保配置完全生效！"), false);
    }
} 