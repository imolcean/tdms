package io.github.imolcean.tdms.core.services.proxies;

import io.github.imolcean.tdms.api.DataSourceWrapper;
import io.github.imolcean.tdms.api.dto.TableRuleDto;
import io.github.imolcean.tdms.api.interfaces.generation.generator.DataGenerator;
import io.github.imolcean.tdms.core.services.managers.DataGenerationImplementationManager;
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
