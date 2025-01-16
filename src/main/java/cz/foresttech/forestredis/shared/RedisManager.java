package cz.foresttech.forestredis.shared;

import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.shared.models.RedisConfiguration;
import redis.clients.jedis.*;

import java.util.*;

/**
 * Redisサーバーへの接続を維持し、処理するためのクラス。
 * 自動的に現在のサーバータイプに対応するBungee/Spigotイベントを発生させます。
 * <p>
 * 開発者がチャンネルを購読し、ジェネリックなEventHandlersを使用してそれらをリッスンすることを可能にします。
 */
public class RedisManager {

    /**
     * メインインスタンス
     */
    private static RedisManager api;

    /**
     * プラグインが関連付けられているプラグイン
     */
    private final IForestRedisPlugin plugin;

    /**
     * 認証情報を保存するための構成オブジェクト
     */
    private RedisConfiguration redisConfiguration;

    /**
     * 現在のサーバーの識別子。ネットワーク全体で一意である必要があります。
     */
    private String serverIdentifier;

    /**
     * 購読されたチャンネルのセット
     */
    private final HashSet<String> channels;

    /**
     * 現在のサブスクリプションのリスト
     */
    private final List<Subscription> subscriptions;

    /**
     * 現在のJedisPoolオブジェクト
     */
    private JedisPool jedisPool;

    /**
     * プロセスが閉じる状態にあるかどうか
     */
    private boolean closing;

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link RedisManager}インスタンスを作成するためのコンストラクタメソッド。コンストラクタはチャンネルを購読せず、
     * 将来のために提供されたデータを保存するだけです。
     *
     * @param plugin             インスタンスを取得しようとする元のプラグイン
     * @param serverIdentifier   サーバーの識別子（例： 'Bungee01'）。バグを防ぐために一意である必要があります
     * @param redisConfiguration Redisサーバーの認証情報を含む{@link RedisConfiguration}オブジェクト
     */
    public RedisManager(IForestRedisPlugin plugin, String serverIdentifier, RedisConfiguration redisConfiguration) {
        this.plugin = plugin;
        this.closing = false;

        this.serverIdentifier = serverIdentifier;
        this.redisConfiguration = redisConfiguration;

        this.subscriptions = new ArrayList<>();

        this.channels = new HashSet<>();
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 既に購読されているチャンネルを保持する場合にマネージャーをリロードします。
     *
     * @param serverIdentifier  新しいサーバー識別子（nullの場合、既に使用しているサーバーIDが使用されます）
     * @param redisConfiguration    新しいRedisConfiguration（nullの場合、既に使用している構成が使用されます）
     * @param keepChannels  既に購読されているチャンネルを保持する
     */
    public void reload(String serverIdentifier, RedisConfiguration redisConfiguration, boolean keepChannels) {
        this.close();
        this.closing = false;

        if (serverIdentifier != null) {
            this.serverIdentifier = serverIdentifier;
        }
        if (redisConfiguration != null) {
            this.redisConfiguration = redisConfiguration;
        }

        if (keepChannels) {
            String[] channels = this.channels.toArray(String[]::new);
            this.channels.clear();
            this.setup(channels);
            return;
        }

        this.setup();
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link #redisConfiguration}から{@link JedisPool}を確立するための主要なセットアップメソッド。
     * メソッドは自動的に{@link #channels}を購読します。
     * <p>
     * 注意！サブスクリプション自体が失敗した場合でも、呼び出しは非同期で行われるため、falseを返しません。
     *
     * @param channels デフォルトのリスニングチャンネルのリスト（大文字小文字を区別）、空にして後で提供することもできます
     * @return セットアップが成功したかどうか
     * @see #subscribe(String...)
     */
    public boolean setup(String... channels) {
        // RedisConfigurationの存在を確認する
        if (this.redisConfiguration == null) {
            plugin.logger().warning("Jedisプールを確立できません！構成がnullであってはなりません！");
            return false;
        }

        // JedisPoolを構築する
        this.jedisPool = this.redisConfiguration.build();
        if (this.jedisPool == null) {
            plugin.logger().warning("提供された構成からJedisプールを確立できません！");
            return false;
        }

        this.plugin.logger().info("サーバー識別子 '" + this.serverIdentifier + "' でJedisプールが確立されました！");

        // チャンネルが提供された場合、それらをリストに追加し、購読する
        if (channels != null && channels.length > 0) {
            this.channels.addAll(Set.of(channels));

            Subscription subscription = new Subscription(this.channels.toArray(new String[0]));
            this.plugin.runAsync(subscription);
            this.subscriptions.add(subscription);
        }

        return true;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 指定されたチャンネルを購読解除します。
     *
     * @param channels 購読解除するチャンネルの名前（大文字小文字を区別）
     */
    public void unsubscribe(String... channels) {
        if (this.closing) {
            return;
        }

        if (channels == null || channels.length == 0) {
            return;
        }

        try {
            for (Subscription sub : this.subscriptions) {
                sub.unsubscribe(channels);
            }
            this.plugin.logger().info("チャンネルの購読解除に成功しました: " + Arrays.toString(channels) + "!");
        } catch (Exception ex) {
            this.plugin.logger().warning("チャンネルの購読解除中にエラーが発生しました: " + Arrays.toString(channels) + "!");
            return;
        }

        this.channels.removeAll(Set.of(channels));
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 既に購読されていない場合、提供されたチャンネルを購読します。対応するイベントは、
     * 受信したメッセージが購読されたチャンネルに送信された場合にのみ発生します。
     *
     * @param channels 購読するチャンネルの名前（大文字小文字を区別）
     * @return 少なくとも1つのチャンネルが正常に購読されたかどうか
     */
    public boolean subscribe(String... channels) {
        if (this.closing) {
            return false;
        }

        if (channels == null || channels.length == 0) {
            return false;
        }

        Set<String> actualChannelsToAdd = new HashSet<>();

        for (String channel : channels) {
            if (this.channels.contains(channel) && channel != null) {
                continue;
            }
            actualChannelsToAdd.add(channel);
        }

        // ユーザーが実際に購読するチャンネルを提供したかどうかを確認する
        if (actualChannelsToAdd.isEmpty()) {
            return false;
        }

        this.channels.addAll(actualChannelsToAdd);

        Subscription subscription = new Subscription(actualChannelsToAdd.toArray(new String[0]));
        this.plugin.runAsync(subscription);
        subscriptions.add(subscription);

        return true;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 提供されたチャンネルにオブジェクトを公開します。サーバー識別も処理します。
     * 単純な{@link String}メッセージを公開するためにこれを使用しないでください。
     *
     * @param targetChannel   公開するチャンネル（大文字小文字を区別）
     * @param objectToPublish 公開するオブジェクト
     * @return メッセージがJSONに変換できない場合や閉じる状態にある場合は 'false' を返します。プロセスが成功した場合は 'true' を返します
     * @see #publishMessage(String, String)
     */
    public boolean publishObject(String targetChannel, Object objectToPublish) {
        MessageTransferObject messageTransferObject = MessageTransferObject.wrap(this.serverIdentifier, objectToPublish, System.currentTimeMillis());
        return this.executePublish(targetChannel, messageTransferObject);
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 提供されたチャンネルにメッセージを公開します。サーバー識別も処理します。
     * このメソッドはシリアル化されたオブジェクトを公開するためには推奨されません。オブジェクトを公開するには：
     *
     * @param targetChannel    公開するチャンネル（大文字小文字を区別）
     * @param messageToPublish 公開するメッセージ
     * @return メッセージがJSONに変換できない場合や閉じる状態にある場合は 'false' を返します。プロセスが成功した場合は 'true' を返します。
     * @see #publishObject(String, Object)
     */
    public boolean publishMessage(String targetChannel, String messageToPublish) {
        MessageTransferObject messageTransferObject = new MessageTransferObject(this.serverIdentifier, messageToPublish, System.currentTimeMillis());
        return this.executePublish(targetChannel, messageTransferObject);
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link MessageTransferObject}オブジェクトを公開するための内部メソッド。
     *
     * @param targetChannel         公開するチャンネル
     * @param messageTransferObject 公開する{@link MessageTransferObject}オブジェクト
     * @return 提供された{@link MessageTransferObject}が意味をなすかどうか
     */
    private boolean executePublish(String targetChannel, MessageTransferObject messageTransferObject) {
        if (this.closing) {
            return false;
        }

        if (messageTransferObject == null) {
            return false;
        }

        String messageJson = messageTransferObject.toJson();
        if (messageJson == null) {
            return false;
        }

        this.plugin.runAsync(() -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                jedis.publish(targetChannel, messageJson);
            } catch (Exception e) {
                RedisManager.this.plugin.logger().warning("Redisサーバーにメッセージを送信できませんでした！");
            }
        });

        return true;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * Redis接続を閉じ、すべてのチャンネルの購読を解除します。
     */
    public void close() {
        if (this.closing) {
            return;
        }

        this.closing = true;
        for (Subscription sub : this.subscriptions) {
            if (sub.isSubscribed()) {
                sub.unsubscribe();
            }
        }

        this.subscriptions.clear();

        if (this.jedisPool == null) {
            return;
        }

        this.jedisPool.destroy();
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 現在のサーバー識別子を返します。イベントの送信者名として使用されます。
     *
     * @return サーバー識別子
     */
    public String getServerIdentifier() {
        return serverIdentifier;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * チャンネルが購読されているかどうかを返します。
     *
     * @param channel 確認するチャンネルの名前（大文字小文字を区別）。
     * @return チャンネルが購読されているかどうか
     */
    public boolean isSubscribed(String channel) {
        return this.channels.contains(channel);
    }

    /*----------------------------------------------------------------------------------------------------------*/

    public Jedis getJedis() {
        return this.jedisPool.getResource();
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * すべての購読されたチャンネルのリストを返します。
     *
     * @return すべての購読されたチャンネルのリスト（大文字小文字を区別）
     */
    public Set<String> getSubscribedChannels() {
        return channels;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * PubSub接続を処理するために使用されるプライベートサブスクリプションクラス。
     *
     * @see #subscribe(String...)
     */
    private class Subscription extends JedisPubSub implements Runnable {

        private final String[] channels;

        public Subscription(String[] channels) {
            this.channels = channels;
        }

        @Override
        public void run() {
            boolean firstTry = true;

            while (!RedisManager.this.closing && !Thread.interrupted() && !RedisManager.this.jedisPool.isClosed()) {
                try (Jedis jedis = RedisManager.this.jedisPool.getResource()) {
                    if (firstTry) {
                        RedisManager.this.plugin.logger().info("Redis pubsub接続が確立されました！");
                        firstTry = false;
                    } else {
                        RedisManager.this.plugin.logger().info("Redis pubsub接続が再確立されました！");
                    }

                    try {
                        jedis.subscribe(this, channels); // ブロッキング呼び出し
                        RedisManager.this.plugin.logger().info("チャンネルの購読に成功しました: " + Arrays.toString(channels) + "!");
                    } catch (Exception e) {
                        RedisManager.this.plugin.logger().warning("購読できませんでした！");
                    }
                } catch (Exception e) {
                    if (RedisManager.this.closing) {
                        return;
                    }

                    RedisManager.this.plugin.logger().warning("Redis pubsub接続が切断されました。接続を再開しようとしています！");
                    try {
                        unsubscribe();
                    } catch (Exception ignored) {
                    }

                    // コンソールの大量のスパムを防ぐために5秒間スリープする
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        @Override
        public void onMessage(String channel, String message) {
            if (channel == null || message == null) {
                return;
            }

            MessageTransferObject messageTransferObject = MessageTransferObject.fromJson(message);
            if (messageTransferObject == null) {
                RedisManager.this.plugin.logger().warning("チャンネル '" + channel + "' に送信されたメッセージオブジェクトを取得できません！メッセージ: '" + message + "'");
                return;
            }

            RedisManager.this.plugin.onMessageReceived(channel, messageTransferObject);
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link RedisManager}メインインスタンスを作成するための初期化メソッド。これにより、接続や購読は開始されません。
     *
     * @param plugin             インスタンスを取得しようとする元のプラグイン
     * @param serverIdentifier   サーバーの識別子（例： 'Bungee01'）。バグを防ぐために一意である必要があります
     * @param redisConfiguration Redisサーバーの認証情報を含む{@link RedisConfiguration}オブジェクト
     */
    public static void init(IForestRedisPlugin plugin, String serverIdentifier, RedisConfiguration redisConfiguration) {
        api = new RedisManager(plugin, serverIdentifier, redisConfiguration);
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * {@link RedisManager}オブジェクトのメインインスタンスを取得します。これがAPIメソッドにアクセスするための唯一の推奨アプローチです。
     *
     * @return {@link RedisManager}のメインインスタンス
     */
    public static RedisManager getAPI() {
        return api;
    }

    /*----------------------------------------------------------------------------------------------------------*/
}
