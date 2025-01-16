package cz.foresttech.forestredis.velocity.adapter;

import cz.foresttech.forestredis.shared.adapter.ILoggerAdapter;
import org.slf4j.Logger;

/**
 * VelocityLoggerAdapterクラスは、ILoggerAdapterインターフェースの実装です。
 * SLF4JのLoggerを使用してログメッセージを記録します。
 */
public class VelocityLoggerAdapter implements ILoggerAdapter {

    private final Logger logger;

    /**
     * VelocityLoggerAdapterのインスタンスを構築します。
     *
     * @param logger 使用するLoggerインスタンス
     */
    public VelocityLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }
}
