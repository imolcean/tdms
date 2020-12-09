package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.generation.generator.DataGenerator;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DataGenerationImplementationManager extends AbstractImplementationManager<DataGenerator>
{
    public DataGenerationImplementationManager(SpringPluginManager plugins)
    {
        super(plugins, DataGenerator.class);
    }
}
