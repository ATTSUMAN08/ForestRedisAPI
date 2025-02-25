package cz.foresttech.forestredis.bungee.adapter;

import cz.foresttech.forestredis.shared.adapter.IConfigurationAdapter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Bungeeバージョンの{@link IConfigurationAdapter}の実装
 */
public class BungeeConfigAdapter implements IConfigurationAdapter {

    private final Plugin plugin;
    private String fileName;
    private Configuration configuration;

    /**
     * アダプタのインスタンスを構築します。
     *
     * @param plugin {@link Plugin}インスタンス
     */
    public BungeeConfigAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup(String fileName) {
        this.fileName = fileName;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File file = new File(plugin.getDataFolder(), fileName + ".yml");

        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream(fileName + ".yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                plugin.getLogger().warning("config.ymlを作成できません！このプロキシはRedis通信を処理しません！");
                return;
            }
            loadConfiguration();
        }
        loadConfiguration();
    }

    @Override
    public boolean isSetup() {
        return configuration != null;
    }

    @Override
    public void loadConfiguration() {
        try {
            configuration = ConfigurationProvider
                    .getProvider(YamlConfiguration.class)
                    .load(new File(plugin.getDataFolder(), fileName + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("config.ymlを読み込めません！このプロキシはRedis通信を処理しません！");
            configuration = null;
        }
    }

    @Override
    public String getString(String path, String def) {
        return this.configuration.getString(path, def);
    }

    @Override
    public int getInt(String path, int def) {
        return this.configuration.getInt(path, def);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return this.configuration.getBoolean(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return this.configuration.getStringList(path);
    }

}
