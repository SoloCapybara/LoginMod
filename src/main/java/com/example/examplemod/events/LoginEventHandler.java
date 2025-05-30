package com.example.examplemod.events;

import com.example.examplemod.Config;
import com.example.examplemod.LoginManager;
import com.example.examplemod.model.User;
import net.minecraft.ChatFormatting;
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
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

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
                // 禁止使用物品和与方块互动
                // 修改提示信息
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能进行操作！").withStyle(ChatFormatting.RED));
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

    // 处理玩家破坏方块事件
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (!LoginManager.getInstance().isLoggedIn(player)) {
                // 禁止未登录玩家破坏方块
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能破坏方块！").withStyle(ChatFormatting.RED));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LoginManager.getInstance().logout(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        // 在玩家加入游戏时检查是否已注册/登录
        // 移除对不存在的 loadUser 方法的调用
        // LoginManager.getInstance().loadUser(player.getGameProfile().getId(), player.getGameProfile().getName());

        // 通过 LoginManager 获取用户数据来判断是否已注册
        // 确保玩家是 ServerPlayer 类型
        if (player instanceof ServerPlayer serverPlayer) {
            Optional<User> userOpt = LoginManager.getInstance().getUser(serverPlayer.getUUID());

            if (userOpt.isEmpty() || !LoginManager.getInstance().isLoggedIn(serverPlayer)) { // 检查用户是否存在且是否已登录，使用 ServerPlayer
                // 如果未注册或未登录，发送提示信息
                serverPlayer.sendSystemMessage(Component.literal("请先登录或注册！").withStyle(ChatFormatting.RED));

                // 显示通用的注册和登录命令提示
                serverPlayer.sendSystemMessage(Component.literal("使用 /register <密码> <确认密码> 进行注册").withStyle(ChatFormatting.YELLOW));
                serverPlayer.sendSystemMessage(Component.literal("使用 /login <密码> 进行登录！").withStyle(ChatFormatting.YELLOW));

                // 根据玩家是否为OP显示不同的帮助信息
                if (serverPlayer.hasPermissions(4)) { // 检查是否为OP (权限等级4)
                    // 管理员显示普通玩家和管理员的帮助提示
                    serverPlayer.sendSystemMessage(Component.literal("您可以使用 /login help 命令查看登录/注册帮助。").withStyle(ChatFormatting.YELLOW));
                    serverPlayer.sendSystemMessage(Component.literal("您可以使用 /loginconfig help 命令查看配置命令帮助。").withStyle(ChatFormatting.YELLOW));
                } else {
                    // 普通玩家只显示普通玩家的帮助提示
                    serverPlayer.sendSystemMessage(Component.literal("您可以使用 /login help 命令查看登录/注册帮助。").withStyle(ChatFormatting.YELLOW));
                }

                // 记录未登录玩家的初始位置，用于移动限制
                LoginManager.getInstance().setInitialPosition(serverPlayer.getUUID(), serverPlayer.position());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;
            if (!player.level().isClientSide()) { // 只在服务器端执行
                // 确保玩家是 ServerPlayer 类型
                if (player instanceof ServerPlayer serverPlayer) {
                    if (!LoginManager.getInstance().isLoggedIn(serverPlayer)) { // 使用 ServerPlayer
                        // 未登录玩家的处理

                        // 添加日志：检查是否进入未登录玩家处理逻辑
                        System.out.println("LoginMod Debug: Unlogged in player tick for " + serverPlayer.getName().getString());

                        // 根据配置施加失明效果
                        if (Config.shouldApplyBlindness()) {
                            // 施加失明效果 (效果ID, 持续时间, 等级, 是否显示粒子, 是否显示图标)
                            // 持续时间设为200 tick (10秒)，等级为0，不显示粒子和图标，以便不太干扰玩家
                            // 如果玩家已经有失明效果且持续时间够长，则不重复施加，避免闪烁
                            if (!serverPlayer.hasEffect(MobEffects.BLINDNESS) || (serverPlayer.getEffect(MobEffects.BLINDNESS) != null && serverPlayer.getEffect(MobEffects.BLINDNESS).getDuration() < 100)) { // 检查是否有效果或持续时间是否较短
                                serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                            }
                        }

                        // 根据配置禁止移动 (瞬移回初始位置)
                        if (Config.shouldRestrictMovement()) {
                             // 添加日志：检查是否进入限制移动逻辑
                             System.out.println("LoginMod Debug: Restrict movement enabled for " + serverPlayer.getName().getString());

                             // 获取初始位置
                             Optional<Vec3> initialPositionOpt = LoginManager.getInstance().getInitialPosition(serverPlayer.getUUID());

                             if (initialPositionOpt.isPresent()) {
                                 Vec3 initialPosition = initialPositionOpt.get();
                                 // 计算当前位置与初始位置的水平距离
                                 double distanceSq = serverPlayer.position().subtract(initialPosition).horizontalDistanceSqr();

                                 // 设置瞬移阈值（例如 1.0，即移动超过 1 个方块的距离）
                                 double teleportThresholdSq = 1.0 * 1.0; // 1个方块的平方

                                 if (distanceSq > teleportThresholdSq) {
                                     // 添加日志：检测到移动超过阈值，进行瞬移
                                     System.out.println("LoginMod Debug: Detected movement beyond threshold for " + serverPlayer.getName().getString() + ", distanceSq: " + distanceSq + ". Teleporting back to " + initialPosition);

                                     // 瞬移回初始位置
                                     serverPlayer.teleportTo(initialPosition.x, initialPosition.y, initialPosition.z);

                                     // 发送提示信息
                                     if (LoginManager.getInstance().canSendMoveWarning(serverPlayer.getUUID())) { // 继续使用冷却机制发送警告
                                         serverPlayer.sendSystemMessage(Component.literal("§c请先登录才能移动！").withStyle(ChatFormatting.RED));
                                         System.out.println("LoginMod Debug: Sent move warning to " + serverPlayer.getName().getString());
                                     }
                                 }
                             } else {
                                 // 如果没有初始位置记录，可能是玩家刚进入或出现问题，记录当前位置作为初始位置
                                 System.out.println("LoginMod Debug: Initial position not found for " + serverPlayer.getName().getString() + ". Setting current position as initial.");
                                 LoginManager.getInstance().setInitialPosition(serverPlayer.getUUID(), serverPlayer.position());
                             }
                         } else {
                            // 添加日志：限制移动配置未启用
                            System.out.println("LoginMod Debug: Restrict movement config is FALSE");
                         }
                    }
                }
            }
        }
    }
} 