package dev.isnow.keepscanner.util;

import lombok.experimental.UtilityClass;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class FileUtil {

    public YamlConfiguration createDB() throws IOException, InvalidConfigurationException {
        File serverFile = new File("servers.yml");

        if(!serverFile.exists()) {
            serverFile.createNewFile();
        }

        YamlConfiguration config = new YamlConfiguration();
        config.load(serverFile);
        return config;
    }

}
