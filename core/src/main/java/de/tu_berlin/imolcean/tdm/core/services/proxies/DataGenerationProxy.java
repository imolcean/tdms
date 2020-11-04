package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator.DataGenerator;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataGenerationImplementationManager;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DataGenerationProxy extends AbstractPublicInterfaceProxy<DataGenerator> implements DataGenerator
{
    public DataGenerationProxy(DataGenerationImplementationManager manager)
    {
        super(manager, DataGenerator.class);
    }

    @Override
    public void generate(DataSourceWrapper ds, Collection<TableRuleDto> rules) throws Exception
    {
        getImplementation().generate(ds, rules);
    }
}
