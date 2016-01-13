package tech.rayline.core.parse;

import org.yaml.snakeyaml.Yaml;
import tech.rayline.core.plugin.RedemptivePlugin;
import tech.rayline.core.plugin.YAMLConfigurationFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public final class SnakeYAMLResourceFileHook implements ResourceFileHook<YAMLConfigurationFile> {
    private final Yaml snakeYaml = new Yaml();

    @Override
    public <T> T read(RedemptivePlugin plugin, File file, Class<T> type) throws Exception {
        return snakeYaml.loadAs(new FileReader(file), type);
    }

    @Override
    public YAMLConfigurationFile readRaw(RedemptivePlugin plugin, File file) throws Exception {
        return new YAMLConfigurationFile(plugin, file);
    }

    @Override
    public void write(RedemptivePlugin plugin, Object o, File file) throws Exception {
        snakeYaml.dump(o, new FileWriter(file));
    }

    @Override
    public void writeRaw(RedemptivePlugin plugin, Object yamlConfigurationFile, File file) throws Exception {
        ((YAMLConfigurationFile) yamlConfigurationFile).saveConfig();
    }
}
