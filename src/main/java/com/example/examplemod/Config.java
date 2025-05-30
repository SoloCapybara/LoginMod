package com.example.examplemod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<StorageType> STORAGE_TYPE = BUILDER
            .comment("选择数据存储方式。强烈不建议在生产环境中使用文件存储，仅推荐在测试时使用，因为它可能存在性能和数据丢失问题。")
            .defineEnum("storageType", StorageType.FILE);

    public static final ForgeConfigSpec.ConfigValue<String> DATABASE_URL = BUILDER
            .comment("数据库连接URL（当使用数据库存储时）。修改此项后，强烈建议重启服务器以确保连接完全生效。")
            .define("databaseUrl", "jdbc:mysql://localhost:3306/minecraft");

    public static final ForgeConfigSpec.ConfigValue<String> DATABASE_USERNAME = BUILDER
            .comment("数据库用户名（当使用数据库存储时）。修改此项后，强烈建议重启服务器以确保连接完全生效。")
            .define("databaseUsername", "root");

    public static final ForgeConfigSpec.ConfigValue<String> DATABASE_PASSWORD = BUILDER
            .comment("数据库密码（当使用数据库存储时）。修改此项后，强烈建议重启服务器以确保连接完全生效。")
            .define("databasePassword", "");

    // 新增配置项：是否禁止未登录玩家移动
    public static final ForgeConfigSpec.BooleanValue RESTRICT_MOVEMENT = BUILDER
            .comment("是否禁止未登录玩家移动。")
            .define("restrictMovement", true); // 默认为 true

    // 新增配置项：是否对未登录玩家施加失明效果
    public static final ForgeConfigSpec.BooleanValue APPLY_BLINDNESS = BUILDER
            .comment("是否对未登录玩家施加失明效果。")
            .define("applyBlindness", true); // 默认为 true

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static StorageType storageType;
    private static String databaseUrl;
    private static String databaseUsername;
    private static String databasePassword;
    private static boolean restrictMovement; // 新增私有变量
    private static boolean applyBlindness; // 新增私有变量

    public enum StorageType {
        FILE,
        DATABASE
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        loadConfig();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event)
    {
        loadConfig();
    }

    private static void loadConfig()
    {
        storageType = STORAGE_TYPE.get();
        databaseUrl = DATABASE_URL.get();
        databaseUsername = DATABASE_USERNAME.get();
        databasePassword = DATABASE_PASSWORD.get();
        restrictMovement = RESTRICT_MOVEMENT.get(); // 加载新配置
        applyBlindness = APPLY_BLINDNESS.get(); // 加载新配置
    }

    public static void reload()
    {
        loadConfig();
    }

    public static StorageType getStorageType()
    {
        return storageType;
    }

    public static void setStorageType(StorageType type)
    {
        STORAGE_TYPE.set(type);
        storageType = type;
    }

    public static String getDatabaseUrl()
    {
        return databaseUrl;
    }

    public static void setDatabaseUrl(String url)
    {
        DATABASE_URL.set(url);
        databaseUrl = url;
    }

    public static String getDatabaseUsername()
    {
        return databaseUsername;
    }

    public static void setDatabaseUsername(String username)
    {
        DATABASE_USERNAME.set(username);
        databaseUsername = username;
    }

    public static String getDatabasePassword()
    {
        return databasePassword;
    }

    public static void setDatabasePassword(String password)
    {
        DATABASE_PASSWORD.set(password);
        databasePassword = password;
    }

    // 新增 getter 方法
    public static boolean shouldRestrictMovement()
    {
        return restrictMovement;
    }

    // 新增 setter 方法
    public static void setRestrictMovement(boolean value)
    {
        RESTRICT_MOVEMENT.set(value);
        restrictMovement = value;
    }

    // 新增 getter 方法
    public static boolean shouldApplyBlindness()
    {
        return applyBlindness;
    }

    // 新增 setter 方法
    public static void setApplyBlindness(boolean value)
    {
        APPLY_BLINDNESS.set(value);
        applyBlindness = value;
    }
}
