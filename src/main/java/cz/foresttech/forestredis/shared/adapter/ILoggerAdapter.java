package cz.foresttech.forestredis.shared.adapter;

/**
 * ロガーアダプターインターフェース
 */
public interface ILoggerAdapter {

    /**
     * 情報メッセージをログに記録します。
     *
     * @param message ログに記録するメッセージ
     */
    void info(String message);

    /**
     * 警告メッセージをログに記録します。
     *
     * @param message ログに記録するメッセージ
     */
    void warning(String message);

    /**
     * エラーメッセージをログに記録します。
     *
     * @param message ログに記録するメッセージ
     */
    void error(String message);

}
