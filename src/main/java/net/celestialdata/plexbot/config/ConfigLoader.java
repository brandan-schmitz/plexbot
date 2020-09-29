package net.celestialdata.plexbot.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * Loads the configuration files and builds a ConfigurationProvider object
 *
 * @author Celestialdeath99
 */
class ConfigLoader {

    /**
     * Loads the configuration files from the filesystem
     *
     * @return Returns the configuration provider
     */
    static ConfigurationProvider configurationProvider() {
        // Set the name, location, and source of the configuration file
        ConfigFilesProvider configFilesProvider = () -> Collections.singletonList(Paths.get("config.yaml"));
        Environment environment = new ImmutableEnvironment(System.getProperty("user.dir") + "/");
        ConfigurationSource source = new FilesConfigurationSource(configFilesProvider);

        // Build the configuration file provider
        return new ConfigurationProviderBuilder()
                .withConfigurationSource(source)
                .withEnvironment(environment)
                .build();
    }
}
