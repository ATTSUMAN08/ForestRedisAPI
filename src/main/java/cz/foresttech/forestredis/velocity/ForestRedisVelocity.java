package cz.foresttech.forestredis.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import cz.foresttech.forestredis.shared.IForestRedisPlugin;
import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.shared.adapter.ILoggerAdapter;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.velocity.adapter.VelocityConfigAdapter;
import cz.foresttech.forestredis.velocity.adapter.VelocityLoggerAdapter;
import cz.foresttech.forestredis.velocity.events.RedisMessageReceivedEvent;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * 設定ファイルを使用して{@link RedisManager}を設定するためのBootstrap Velocityプラグイン。
 * また、サーバーにリロードとバージョンコマンドを提供します。
 */
@Plugin(
        id = "forestredisapi",
        name = "ForestRedisAPI",
        version = "1.0.0",
        description = "Powerful and simple Redis API by ForestTech",
        authors = "ForestTech"
)
public class ForestRedisVelocity implements IForestRedisPlugin {

    private static ForestRedisVelocity instance;

    private final ProxyServer server;
    private final ILoggerAdapter loggerAdapter;
    private final Path dataDirectory;

    @Inject
    public ForestRedisVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.server = server;
        this.loggerAdapter = new VelocityLoggerAdapter(logger);
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        load();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // RedisManagerを閉じる
        if (RedisManager.getAPI() == null) {
            return;
        }
        RedisManager.getAPI().close();
    }

    @Override
    public void runAsync(Runnable task) {
        server.getScheduler().buildTask(this, task).schedule();
    }

    @Override
    public void onMessageReceived(String channel, MessageTransferObject messageTransferObject) {
        server.getEventManager().fire(new RedisMessageReceivedEvent(channel, messageTransferObject)).thenAccept((event) -> {});
    }

    @Override
    public ILoggerAdapter logger() {
        return loggerAdapter;
    }

    public PluginDescription getDescription() {
        PluginContainer pluginContainer = server.getPluginManager().getPlugin("forestredisapi").orElse(null);
        if (pluginContainer == null) {
            return null;
        }

        return pluginContainer.getDescription();
    }

    @Override
    public IConfigurationAdapter getConfigAdapter() {
        VelocityConfigAdapter velocityConfigAdapter = new VelocityConfigAdapter(this);
        velocityConfigAdapter.setup("config");
        return velocityConfigAdapter;
    }

    /**
     * プラグインのインスタンスを取得します
     *
     * @return {@link ForestRedisVelocity}のインスタンス
     */
    public static ForestRedisVelocity getInstance() {
        return instance;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ProxyServer getServer() {
        return server;
    }
}
