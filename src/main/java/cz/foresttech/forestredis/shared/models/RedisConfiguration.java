package cz.foresttech.forestredis.shared.models;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * RedisConfigurationオブジェクトは、Redisサーバーの認証情報を保存します。JedisPoolインスタンスを作成するために使用できます。
 */
public class RedisConfiguration {

    private final String hostName;
    private final int port;
    private final String username;
    private final String password;
    private final boolean ssl;

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * オブジェクトのインスタンスを構築します
     *
     * @param hostName Redisサーバーのホスト名
     * @param port     Redisサーバーのポート
     * @param username Redisサーバーに接続するために使用するユーザー名（nullも可能）
     * @param password Redisサーバーに接続するために使用するパスワード（nullも可能）
     * @param ssl      SSLで接続するかどうか
     */
    public RedisConfiguration(String hostName, int port, String username, String password, boolean ssl) {
        this.hostName = hostName;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ssl = ssl;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 保存された値を使用して{@link JedisPool}インスタンスを作成します。
     *
     * @return このオブジェクト内の値を使用して取得された{@link JedisPool}オブジェクト。
     */
    public JedisPool build() {
        // ホスト名は必須です！
        if (hostName == null) {
            return null;
        }

        try {
            if (this.username == null) {
                return new JedisPool(new JedisPoolConfig(), this.hostName, this.port, Protocol.DEFAULT_TIMEOUT, this.password, this.ssl);
            }
            return new JedisPool(new JedisPoolConfig(), this.hostName, this.port, Protocol.DEFAULT_TIMEOUT, this.username, this.password, this.ssl);
        } catch (Exception exception) {
            return null;
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/

}
