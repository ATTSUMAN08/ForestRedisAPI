package cz.foresttech.forestredis.spigot;

import cz.foresttech.forestredis.shared.*;
import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.shared.adapter.ILoggerAdapter;
import cz.foresttech.forestredis.shared.adapter.JUtilLoggerAdapter;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.spigot.adapter.SpigotConfigAdapter;
import cz.foresttech.forestredis.spigot.events.AsyncRedisMessageReceivedEvent;
import cz.foresttech.forestredis.spigot.events.RedisMessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 設定ファイルを使用して{@link RedisManager}を設定するためのBootstrap Spigotプラグイン。
 * また、サーバーにリロードとバージョンコマンドを提供します。
 */
public class ForestRedisSpigot extends JavaPlugin implements IForestRedisPlugin {

    private static ForestRedisSpigot instance;

    private ILoggerAdapter loggerAdapter;

    @Override
    public void onEnable() {
        instance = this;
        loggerAdapter = new JUtilLoggerAdapter(getLogger());

        load();
    }

    @Override
    public void onDisable() {
        if (RedisManager.getAPI() == null) {
            return;
        }
        RedisManager.getAPI().close();
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, task);
    }

    @Override
    public void onMessageReceived(String channel, MessageTransferObject messageTransferObject) {
        AsyncRedisMessageReceivedEvent asyncRedisMessageReceivedEvent = new AsyncRedisMessageReceivedEvent(channel, messageTransferObject);
        Bukkit.getPluginManager().callEvent(asyncRedisMessageReceivedEvent);

        if (!asyncRedisMessageReceivedEvent.isCancelled()) {
            Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().callEvent(new RedisMessageReceivedEvent(asyncRedisMessageReceivedEvent.getChannel(), asyncRedisMessageReceivedEvent.getMessageTransferObject())));
        }
    }

    @Override
    public ILoggerAdapter logger() {
        return loggerAdapter;
    }

    @Override
    public IConfigurationAdapter getConfigAdapter() {
        SpigotConfigAdapter spigotConfigAdapter = new SpigotConfigAdapter(this);
        spigotConfigAdapter.setup("config");
        return spigotConfigAdapter;
    }

    /**
     * プラグインのインスタンスを取得します
     *
     * @return {@link ForestRedisSpigot}のインスタンス
     */
    public static ForestRedisSpigot getInstance() {
        return instance;
    }
}
