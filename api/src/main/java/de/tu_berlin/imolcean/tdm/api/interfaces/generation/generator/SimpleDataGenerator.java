package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;

import java.util.Collection;

/**
 * Simplified version of {@link DataGenerator} that is able to generate data without any rules specified.
 */
public interface SimpleDataGenerator extends DataGenerator
{
    /**
     * Generates synthetic data for the specified database.
     *
     * In order to save RAM, generated data is not returned but directly written into the database. This allows
     * using this generator on large schemas and generate large amount of data. However, RAM may be used to store
     * generated data of one table at a time, that is the table that is being handled at the moment. It means,
     * there should be enough memory to store the generated data of one whole table. If it is not the case, one can run
     * the generation process several times.
     *
     * @param ds database that should contain the generated data
     */
    void generate(DataSourceWrapper ds) throws Exception;

    @Override
    default void generate(DataSourceWrapper ds, Collection<TableRuleDto> rules) throws Exception
    {
        generate(ds);
    }
}
