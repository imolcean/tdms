package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;

import java.util.Collection;

public interface SimpleDataGenerator extends DataGenerator
{
    void generate(DataSourceWrapper ds) throws Exception;

    @Override
    default void generate(DataSourceWrapper ds, Collection<?> params) throws Exception
    {
        generate(ds);
    }
}
