package cz.foresttech.forestredis.shared.events;

/**
 * Redisの受信メッセージイベントインターフェース。SpigotとBungeeのイベントAPIの違いを処理するために使用されます。
 */
public interface IRedisMessageReceivedEvent {

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 送信者の識別子を取得します。
     *
     * @return 送信サーバーの名前
     */
    String getSenderIdentifier();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージが送信されたチャンネルの名前を取得します。
     *
     * @return 受信チャンネルの名前
     */
    String getChannel();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 受信したメッセージを取得します。
     *
     * @return 受信したメッセージ
     */
    String getMessage();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージのタイムスタンプを取得します。
     *
     * @return メッセージのタイムスタンプ
     */
    long getTimeStamp();

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 受信したメッセージから指定された型のオブジェクトを取得します。
     *
     * @param objectClass オブジェクトのクラス
     * @param <T> オブジェクトの型
     * @return 解析されたオブジェクト（解析できない場合はnull）
     */
    @SuppressWarnings("受信したメッセージが指定された型に変換できることを確認してください！")
    <T> T getMessageObject(Class<T> objectClass);

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 送信サーバーが現在のサーバーと同じ識別子を持っているかどうかを確認します。
     *
     * @return メッセージがこのサーバーから送信されたかどうか
     */
    boolean isSelfSender();

    /*----------------------------------------------------------------------------------------------------------*/

}
