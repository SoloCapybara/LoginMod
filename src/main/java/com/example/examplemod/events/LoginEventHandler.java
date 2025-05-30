package com.example.examplemod.events;

import com.example.examplemod.LoginManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;

@Mod.EventBusSubscriber
public class LoginEventHandler {
    
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!LoginManager.getInstance().isLoggedIn(player)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("§c请先登录后再发言！"));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止使用物品
                if (event.getHand() == InteractionHand.MAIN_HAND) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c请先登录后再使用物品！"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止受到伤害
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止受到伤害
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止与方块交互
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录后再与方块交互！"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止与实体交互
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录后再与实体交互！"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractWithItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止使用物品
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录后再使用物品！"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager.getInstance().logout(player);
        }
    }
} 