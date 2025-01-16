package cz.foresttech.forestredis.velocity.events;

import cz.foresttech.forestredis.shared.RedisManager;
import cz.foresttech.forestredis.shared.events.IRedisMessageReceivedEvent;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;

/**
 * 購読されたチャンネルからメッセージが受信されたときに使用されるVelocityイベントクラス。
 */
public class RedisMessageReceivedEvent implements IRedisMessageReceivedEvent {

    /**
     * メッセージが送信されたチャンネルの名前
     */
    private final String channel;

    /**
     * メッセージのデータを含むMessageTransferObject
     */
    private final MessageTransferObject messageTransferObject;

    /**
     * イベントのインスタンスを構築します
     *
     * @param channel   メッセージが公開されたチャンネル
     * @param messageTransferObject 公開されたメッセージに関するデータを含む{@link MessageTransferObject}オブジェクト
     */
    public RedisMessageReceivedEvent(String channel, MessageTransferObject messageTransferObject) {
        this.channel = channel;
        this.messageTransferObject = messageTransferObject;
    }

    @Override
    public String getSenderIdentifier() {
        return this.messageTransferObject.getSenderIdentifier();
    }

    @Override
    public String getChannel() {
        return this.channel;
    }

    @Override
    public String getMessage() {
        return this.messageTransferObject.getMessage();
    }

    @Override
    public <T> T getMessageObject(Class<T> objectClass) {
        return this.messageTransferObject.parseMessageObject(objectClass);
    }

    @Override
    public boolean isSelfSender() {
        return this.messageTransferObject.getSenderIdentifier().equals(RedisManager.getAPI().getServerIdentifier());
    }

    @Override
    public long getTimeStamp() {
        return this.messageTransferObject.getTimestamp();
    }

}
