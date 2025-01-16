package cz.foresttech.forestredis.bungee;

import cz.foresttech.forestredis.bungee.adapter.BungeeConfigAdapter;
import cz.foresttech.forestredis.bungee.events.RedisMessageReceivedEvent;
import cz.foresttech.forestredis.shared.*;
import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.shared.adapter.ILoggerAdapter;
import cz.foresttech.forestredis.shared.adapter.JUtilLoggerAdapter;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * {@link RedisManager}を設定するためのBootstrap BungeeCordプラグイン。
 * 設定ファイルを使用します。また、サーバーにリロードとバージョンコマンドを提供します。
 */
public class ForestRedisBungee extends Plugin implements IForestRedisPlugin {
    private static ForestRedisBungee instance;

    private ILoggerAdapter loggerAdapter;

    @Override
    public void onEnable() {
        instance = this;
        loggerAdapter = new JUtilLoggerAdapter(getLogger());

        load();
    }

    @Override
    public void onDisable() {
        // RedisManagerを閉じる
        if (RedisManager.getAPI() == null) {
            return;
        }
        RedisManager.getAPI().close();
    }

    @Override
    public void runAsync(Runnable task) {
        ProxyServer.getInstance().getScheduler().runAsync(instance, task);
    }

    @Override
    public void onMessageReceived(String channel, MessageTransferObject messageTransferObject) {
        ProxyServer.getInstance().getPluginManager().callEvent(new RedisMessageReceivedEvent(channel, messageTransferObject));
    }

    @Override
    public ILoggerAdapter logger() {
        return loggerAdapter;
    }

    @Override
    public IConfigurationAdapter getConfigAdapter() {
        BungeeConfigAdapter bungeeConfigAdapter = new BungeeConfigAdapter(this);
        bungeeConfigAdapter.setup("config");
        return bungeeConfigAdapter;
    }

    /**
     * プラグインのインスタンスを取得します
     *
     * @return {@link ForestRedisBungee}のインスタンス
     */
    public static ForestRedisBungee getInstance() {
        return instance;
    }
}
