package io.github.imolcean.tdms.core.plugins;

import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SingletonSpringExtensionFactory;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Properties;

@Log
public class TdmExtensionFactory extends SingletonSpringExtensionFactory
{
    private final boolean autowire;
    private final PluginManager pluginManager;
    private final Path configsRoot;

    public TdmExtensionFactory(PluginManager pluginManager, Path configsRoot)
    {
        super(pluginManager);
        this.autowire = true;
        this.pluginManager = pluginManager;
        this.configsRoot = configsRoot;
    }

    @Override
    public <T> T create(Class<T> extensionClass)
    {
        T extension = createWithoutSpring(extensionClass);

        if (autowire && extension != null)
        {
            // test for SpringBean
            PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);

            if(pluginWrapper != null)
            { // is plugin extension
                Plugin plugin = pluginWrapper.getPlugin();

                if (plugin instanceof SpringPlugin)
                {
                    // autowire
                    ApplicationContext pluginContext = ((SpringPlugin) plugin).getApplicationContext();
                    pluginContext.getAutowireCapableBeanFactory().autowireBean(extension);
                }
                else if(this.pluginManager instanceof SpringPluginManager)
                {
                    // is system extension and plugin manager is SpringPluginManager
                    SpringPluginManager springPluginManager = (SpringPluginManager) this.pluginManager;
                    ApplicationContext pluginContext = springPluginManager.getApplicationContext();
                    pluginContext.getAutowireCapableBeanFactory().autowireBean(extension);
                }
            }
            else if(this.pluginManager instanceof SpringPluginManager)
            {
                // is system extension and plugin manager is SpringPluginManager
                SpringPluginManager springPluginManager = (SpringPluginManager) this.pluginManager;
                ApplicationContext pluginContext = springPluginManager.getApplicationContext();
                pluginContext.getAutowireCapableBeanFactory().autowireBean(extension);
            }
        }

        return extension;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T createWithoutSpring(Class<?> extensionClass)
    {
        try
        {
            Properties props = extractPropertiesFromConfig(findExtensionConfig(extensionClass));
            Constructor<?> ctr = extensionClass.getConstructor(Properties.class);

            return (T) ctr.newInstance(props);
        }
        catch(IOException e)
        {
            log.warning("Could not load configuration for " + extensionClass.getName());
        }
        catch(NoSuchMethodException e)
        {
            log.warning(String.format("Extension %s doesn't provide a constructor that takes configuration", extensionClass.getName()));
        }
        catch(IllegalAccessException | InstantiationException | InvocationTargetException e)
        {
            log.warning(String.format("Configuration-aware instantiation of %s failed", extensionClass.getName()));
            e.printStackTrace();
        }

        return super.createWithoutSpring(extensionClass);
    }

    private Path findExtensionConfig(Class<?> extensionClass) throws FileNotFoundException
    {
        log.fine("Looking up configuration for " + extensionClass.getName());

        File config = FileUtils.listFiles(configsRoot.toFile(), new String[]{"properties"}, false).stream()
                .filter(file -> file.getName().equalsIgnoreCase(extensionClass.getName() + ".properties"))
                .findFirst()
                .orElseThrow(FileNotFoundException::new);

        return config.toPath();
    }

    private Properties extractPropertiesFromConfig(Path config) throws IOException
    {
        try(InputStream input = new FileInputStream(config.toFile()))
        {
            Properties props = new Properties();
            props.load(input);

            return props;
        }
    }
}
