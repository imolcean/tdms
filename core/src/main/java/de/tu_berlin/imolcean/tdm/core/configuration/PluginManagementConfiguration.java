package de.tu_berlin.imolcean.tdm.core.configuration;

import de.tu_berlin.imolcean.tdm.core.plugins.TdmExtensionFactory;
import org.pf4j.ExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class PluginManagementConfiguration
{
    @Value("${app.plugins.path}")
    String pluginsDir;

    @Value("${app.plugins.config.path}")
    String configsDir;

    @Bean
    public SpringPluginManager pluginManager()
    {
        return new SpringPluginManager(Path.of(pluginsDir))
        {
            @Override
            protected ExtensionFactory createExtensionFactory()
            {
                return new TdmExtensionFactory(this, Path.of(configsDir));
            }
        };
    }
}
