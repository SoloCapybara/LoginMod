package com.example.examplemod.commands;

import com.example.examplemod.LoginManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
            .then(Commands.argument("password", StringArgumentType.string())
                .then(Commands.argument("confirmPassword", StringArgumentType.string())
                    .executes(context -> {
                        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                            context.getSource().sendFailure(Component.literal("§c该命令只能由玩家执行！"));
                            return 0;
                        }

                        String password = StringArgumentType.getString(context, "password");
                        String confirmPassword = StringArgumentType.getString(context, "confirmPassword");

                        if (!password.equals(confirmPassword)) {
                            context.getSource().sendFailure(Component.literal("§c两次输入的密码不一致！"));
                            return 0;
                        }

                        if (LoginManager.getInstance().register(player, password)) {
                            return 1;
                        }
                        return 0;
                    })))
            .executes(context -> {
                context.getSource().sendFailure(Component.literal("§c用法: /register <密码> <确认密码>"));
                return 0;
            }));

        dispatcher.register(Commands.literal("login")
            .then(Commands.argument("password", StringArgumentType.string())
                .executes(context -> {
                    if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                        context.getSource().sendFailure(Component.literal("§c该命令只能由玩家执行！"));
                        return 0;
                    }

                    String password = StringArgumentType.getString(context, "password");
                    if (LoginManager.getInstance().login(player, password)) {
                        return 1;
                    }
                    return 0;
                }))
            .executes(context -> {
                context.getSource().sendFailure(Component.literal("§c用法: /login <密码>"));
                return 0;
            }));

        dispatcher.register(Commands.literal("logout")
            .executes(context -> {
                if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                    context.getSource().sendFailure(Component.literal("§c该命令只能由玩家执行！"));
                    return 0;
                }

                LoginManager.getInstance().logout(player);
                return 1;
            }));

        // Add help command
        dispatcher.register(Commands.literal("login")
            .then(Commands.literal("help")
                .executes(context -> {
                    if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                        context.getSource().sendFailure(Component.literal("§c该命令只能由玩家执行！"));
                        return 0;
                    }
                    player.sendSystemMessage(Component.literal("§a§l--- 登录/注册帮助 ---"));
                    player.sendSystemMessage(Component.literal("§e/register <密码> <确认密码> §f- 注册账号"));
                    player.sendSystemMessage(Component.literal("§e/login <密码> §f- 登录账号"));
                    player.sendSystemMessage(Component.literal("§e/logout §f- 登出当前账号"));
                    player.sendSystemMessage(Component.literal("§a§l---------------------"));
                    return 1;
                }))
        );
    }
} 