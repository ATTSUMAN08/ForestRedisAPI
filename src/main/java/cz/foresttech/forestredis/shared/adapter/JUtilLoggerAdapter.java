package cz.foresttech.forestredis.shared.adapter;

import java.util.logging.Logger;

/**
 * JUtilLoggerAdapterクラスは、ILoggerAdapterインターフェースの実装です。
 * JavaのLoggerを使用してログメッセージを記録します。
 */
public class JUtilLoggerAdapter implements ILoggerAdapter {

    private final Logger logger;

    /**
     * JUtilLoggerAdapterのインスタンスを構築します。
     *
     * @param logger 使用するLoggerインスタンス
     */
    public JUtilLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }
}
