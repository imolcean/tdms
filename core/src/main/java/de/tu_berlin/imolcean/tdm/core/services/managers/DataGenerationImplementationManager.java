package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator.DataGenerator;
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
