package cz.foresttech.forestredis.shared.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ネットワーク全体でデータを送信するために使用されるDTOオブジェクト。
 * {@link Gson}を使用したカスタムオブジェクトのシリアル化をサポートします。
 */
public class MessageTransferObject {

    private String senderIdentifier;
    private String message;
    private long timestamp;

    /**
     * データを後で追加するためにセッターを使用する場合の空のコンストラクタ
     */
    public MessageTransferObject() {
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 提供されたパラメータでインスタンスを構築します
     *
     * @param senderIdentifier 送信サーバーの識別子
     * @param message          メッセージの内容
     * @param timestamp        メッセージのタイムスタンプ
     */
    public MessageTransferObject(String senderIdentifier, String message, long timestamp) {
        this.senderIdentifier = senderIdentifier;
        this.message = message;
        this.timestamp = timestamp;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 現在のデータを{@link Gson}を使用してJSONに変換します
     *
     * @return JSON文字列にシリアル化されたオブジェクト
     */
    public String toJson() {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(this);
        } catch (Exception ex) {
            return null;
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 提供されたJSON文字列から{@link MessageTransferObject}を取得します。
     *
     * @param json JSON文字列でシリアル化された{@link MessageTransferObject}
     * @return デシリアライズされた{@link MessageTransferObject}
     */
    public static MessageTransferObject fromJson(String json) {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(json, MessageTransferObject.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 指定されたオブジェクトを{@link MessageTransferObject}オブジェクトにラップします。
     *
     * @param senderIdentifier 送信サーバーの識別子
     * @param objectToWrap     ラップするオブジ���クト
     * @param timestamp        メッセージのタイムスタンプ
     * @return 入力からシリアル化されたオブジェクトを含む{@link MessageTransferObject}のインスタンス
     */
    public static MessageTransferObject wrap(String senderIdentifier, Object objectToWrap, long timestamp) {
        Gson gson = new GsonBuilder().create();
        String message = gson.toJson(objectToWrap);

        return new MessageTransferObject(senderIdentifier, message, timestamp);
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージ文字列を指定されたオブジェクトタイプに解析します。
     *
     * @param objectType オブジェクトのクラス
     * @param <T>        オブジェクトのタイプ
     * @return 解析されたオブジェクト、またはオブジェクトを解析できない場合はnull
     */
    public <T> T parseMessageObject(Class<T> objectType) {
        try {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(this.message, objectType);
        } catch (Exception ex) {
            return null;
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージのタイムスタンプを取得します
     *
     * @return メッセージが送信された日時
     */
    public long getTimestamp() {
        return timestamp;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * メッセージのタイムスタンプを変更します
     *
     * @param timestamp メッセージが送信された新しい日時
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * サーバー送信者の識別子を取得します
     *
     * @return 送信者のサーバー識別子
     */
    public String getSenderIdentifier() {
        return senderIdentifier;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 転送されたメッセージの内容を取得します
     *
     * @return メッセージの内容
     */
    public String getMessage() {
        return message;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * サーバー送信者の識別子を変更します
     *
     * @param senderIdentifier 新しいサーバー送信者の識別子
     */
    public void setSenderIdentifier(String senderIdentifier) {
        this.senderIdentifier = senderIdentifier;
    }

    /*----------------------------------------------------------------------------------------------------------*/

    /**
     * 転送されたメッセージの内容を更新します
     *
     * @param message 新しい転送されたメッセージの内容
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /*----------------------------------------------------------------------------------------------------------*/

}
