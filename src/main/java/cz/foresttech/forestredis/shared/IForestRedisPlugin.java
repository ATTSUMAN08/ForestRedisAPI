package cz.foresttech.forestredis.shared;

import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.shared.adapter.ILoggerAdapter;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.shared.models.RedisConfiguration;

import java.util.List;

/**
 * 一般的なプラグインインターフェース。BungeeとSpigotのサーバーエンジン間で同様の機能を処理するために使用されます。
 */
public interface IForestRedisPlugin {

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 非同期タスクを実行します
     *
     * @param task 非同期で実行するタスク
     */
    void runAsync(Runnable task);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージが受信されたときに対応するイベントを呼び出します
     *
     * @param channel               メッセージを受信したチャンネル
     * @param messageTransferObject 受信した{@link MessageTransferObject}
     */
    void onMessageReceived(String channel, MessageTransferObject messageTransferObject);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * ロガーオブジェクトを返します
     *
     * @return 現在の実装のロガーインスタンス
     */
    ILoggerAdapter logger();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link IConfigurationAdapter}の実装を返します
     *
     * @return {@link IConfigurationAdapter}の実装
     */
    default IConfigurationAdapter getConfigAdapter() {
        return null;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * configからRedisManagerを読み込むメソッド
     */
    default void load() {
        // RedisManagerが既に初期化されている場合は閉じる
        if (RedisManager.getAPI() != null) {
            RedisManager.getAPI().close();
        }

        // 設定ファイルを読み込む
        IConfigurationAdapter configAdapter = this.getConfigAdapter();
        if (!configAdapter.isSetup()) {
            return;
        }

        this.logger().info("config.ymlが正常に読み込まれました！");

        // サーバー識別子を読み込む
        String serverIdentifier = configAdapter.getString("serverIdentifier", null);
        if (serverIdentifier == null) {
            serverIdentifier = "MySuperServer1";
            this.logger().info("config.ymlから'serverIdentifier'を読み込めません！'MySuperServer1'を使用します！");
        }

        // RedisConfigurationを構築する
        RedisConfiguration redisConfiguration = new RedisConfiguration(
                configAdapter.getString("redis.hostname", "localhost"),
                configAdapter.getInt("redis.port", 6379),
                configAdapter.getString("redis.username", null),
                configAdapter.getString("redis.password", null),
                configAdapter.getBoolean("redis.ssl", false)
        );


        // RedisManagerを設定する
        List<String> channels = configAdapter.getStringList("channels");

        // RedisManagerオブジェクトを初期化する
        if (RedisManager.getAPI() == null) {
            RedisManager.init(this, serverIdentifier, redisConfiguration);
            if (channels.isEmpty()) {
                RedisManager.getAPI().setup();
                return;
            }

            String[] channelsArray = channels.toArray(new String[0]);
            RedisManager.getAPI().setup(channelsArray);
            return;
        }

        // 既に初期化されている場合はRedisManagerをリロードする
        RedisManager.getAPI().reload(serverIdentifier, redisConfiguration, true);
        if (!channels.isEmpty()) {
            String[] channelsArray = channels.toArray(new String[0]);
            RedisManager.getAPI().subscribe(channelsArray);
        }

    }

    /*----------------------------------------------------------------------------------------------------------*/

}
