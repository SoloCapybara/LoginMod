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
                serverPlayer.sendSystemMessage(Component.literal("使用 /register <密码> 进行注册").withStyle(ChatFormatting.YELLOW));
                serverPlayer.sendSystemMessage(Component.literal("使用 /login <密码> 进行登录！").withStyle(ChatFormatting.YELLOW));
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

                        // 根据配置施加失明效果
                        if (Config.shouldApplyBlindness()) {
                            // 施加失明效果 (效果ID, 持续时间, 等级, 是否显示粒子, 是否显示图标)
                            // 持续时间设为200 tick (10秒)，等级为0，不显示粒子和图标，以便不太干扰玩家
                            // 如果玩家已经有失明效果且持续时间够长，则不重复施加，避免闪烁
                            if (!serverPlayer.hasEffect(MobEffects.BLINDNESS) || (serverPlayer.getEffect(MobEffects.BLINDNESS) != null && serverPlayer.getEffect(MobEffects.BLINDNESS).getDuration() < 100)) { // 检查是否有效果或持续时间是否较短
                                serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                            }
                        }

                        // 根据配置禁止移动 (在 PlayerTickEvent 中检查移动速度并归零)
                        if (Config.shouldRestrictMovement()) {
                             // 检查玩家是否有显著的水平移动速度
                             if (serverPlayer.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) { // 检查水平移动
                                 serverPlayer.setDeltaMovement(0, serverPlayer.getDeltaMovement().y, 0); // 只保留 Y 轴移动 (跳跃/下落)
                                 // 使用冷却机制发送警告
                                 if (LoginManager.getInstance().canSendMoveWarning(serverPlayer.getUUID())) {
                                     serverPlayer.sendSystemMessage(Component.literal("请先登录才能移动！").withStyle(ChatFormatting.RED));
                                 }
                             }
                         }
                    }
                }
            }
        }
    }
} 