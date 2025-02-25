package cz.foresttech.forestredis.velocity.adapter;

import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import cz.foresttech.forestredis.velocity.ForestRedisVelocity;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Velocityバージョンの{@link IConfigurationAdapter}の実装
 */
public class VelocityConfigAdapter implements IConfigurationAdapter {

    private final ForestRedisVelocity plugin;
    private CommentedConfigurationNode configuration;

    private Path configPath;
    private YamlConfigurationLoader loader;

    /**
     * アダプタのインスタンスを構築します。
     *
     * @param plugin {@link ForestRedisVelocity}インスタンス
     */
    public VelocityConfigAdapter(ForestRedisVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup(String fileName) {
        if (Files.notExists(plugin.getDataDirectory())) {
            try {
                Files.createDirectory(plugin.getDataDirectory());
            } catch (IOException e) {
                plugin.logger().warning("プラグインディレクトリを作成できません！このプロキシはRedis通信を処理しません！");
                return;
            }
        }

        configPath = plugin.getDataDirectory().resolve(fileName + ".yml");
        if (Files.notExists(configPath)) {
            try (InputStream stream = plugin.getClass().getClassLoader().getResourceAsStream(fileName + ".yml")) {
                Files.copy(stream, configPath);
            } catch (IOException e) {
                plugin.logger().warning("config.ymlを作成できません！このプロキシはRedis通信を処理しません！");
                return;
            }
        }

        loadConfiguration();
    }

    @Override
    public boolean isSetup() {
        return configuration != null;
    }

    @Override
    public void loadConfiguration() {
        loader = YamlConfigurationLoader.builder().path(configPath).build();
        try {
            configuration = loader.load();
        } catch (ConfigurateException e) {
            plugin.logger().warning("config.ymlを読み込めません！このプロキシはRedis通信を処理しません！");
        }
    }

    @Override
    public String getString(String path, String def) {
        List<String> split = Arrays.stream(path.split("\\.")).toList();
        String result = configuration.node(split).getString();
        if (result == null) {
            return def;
        }
        return result;
    }

    @Override
    public int getInt(String path, int def) {
        List<String> split = Arrays.stream(path.split("\\.")).toList();
        return this.configuration.node(split).getInt(def);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        List<String> split = Arrays.stream(path.split("\\.")).toList();
        return this.configuration.node(split).getBoolean(def);
    }

    @Override
    public List<String> getStringList(String path) {
        List<String> split = Arrays.stream(path.split("\\.")).toList();
        try {
            return this.configuration.node(split).getList(String.class);
        } catch (SerializationException e) {
            return new ArrayList<>();
        }
    }

}
