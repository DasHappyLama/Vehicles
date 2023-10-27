package me.swipez.vehicles.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@SuppressWarnings({"ResultOfMethodCallIgnored", "CallToPrintStackTrace", "unused"})
public class ConfigGenerator {

    private final YamlConfiguration config;

    private final File file;

    public ConfigGenerator(File path, String name) {
        file = new File(path.getPath(), name+".yml");
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void addDefaultToConfig(String configPath, Object value){
        config.addDefault(configPath, value);
    }

    public YamlConfiguration getConfig(){
        return config;
    }

    public void saveConfig() throws IOException {
        config.save(file);
    }

    public void reloadConfig() throws IOException, InvalidConfigurationException {
        config.load(file);
    }

    public void buildConfig(){
        getConfig().options().copyDefaults(true);
        try {
            saveConfig();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
