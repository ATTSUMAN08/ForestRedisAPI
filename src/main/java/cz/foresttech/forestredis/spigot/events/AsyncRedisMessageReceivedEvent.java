package cz.foresttech.forestredis.spigot.events;

import cz.foresttech.forestredis.shared.events.IRedisMessageReceivedEvent;
import cz.foresttech.forestredis.shared.models.MessageTransferObject;
import cz.foresttech.forestredis.shared.RedisManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 購読されたチャンネルからメッセージが受信されたときに使用される非同期のSpigotイベントクラス。
 * <p>
 * データは変更可能であり、更新されたデータで同期Spigotイベントがトリガーされます。
 */
public class AsyncRedisMessageReceivedEvent extends Event implements IRedisMessageReceivedEvent, Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;

    /**
     * メッセージが送信されたチャンネルの名前
     */
    private String channel;

    /**
     * メッセージのデータを含むMessageTransferObject
     */
    private MessageTransferObject messageTransferObject;

    /**
     * イベントのインスタンスを構築します
     *
     * @param channel   メッセージが公開されたチャンネル
     * @param messageTransferObject 公開されたメッセージに関するデータを含む{@link MessageTransferObject}オブジェクト
     */
    public AsyncRedisMessageReceivedEvent(String channel, MessageTransferObject messageTransferObject) {
        super(true);
        this.cancelled = false;
        this.channel = channel;
        this.messageTransferObject = messageTransferObject;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setMessageTransferObject(MessageTransferObject messageTransferObject) {
        this.messageTransferObject = messageTransferObject;
    }

    public MessageTransferObject getMessageTransferObject() {
        return messageTransferObject;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
