package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;

import java.util.Collection;

public interface SimpleDataGenerator extends DataGenerator
{
    void generate(DataSourceWrapper ds) throws Exception;

    @Override
    default void generate(DataSourceWrapper ds, Collection<TableRuleDto> params) throws Exception
    {
        generate(ds);
    }
}
