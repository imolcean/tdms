package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

import java.util.Collection;

/**
 * Represents a piece of TDMS functionality that is responsible for generating synthetic data based on
 * the specified set of rules for every table.
 */
public interface DataGenerator extends PublicInterface, ExtensionPoint
{
    /**
     * Generates synthetic data based on the {@code rules} that are specified for every table that should
     * contain the generated data.
     *
     * In order to save RAM, generated data is not returned but directly written into the database. This allows
     * using this generator on large schemas and generate large amount of data. However, RAM may be used to store
     * generated data of one table at a time, that is the table that is being handled at the moment. It means,
     * there should be enough memory to store the generated data of one whole table. If it is not the case, one can run
     * the generation process several times.
     *
     * @param ds database that should contain the generated data
     * @param rules rules for every table that should receive the generated data
     */
    void generate(DataSourceWrapper ds, Collection<TableRuleDto> rules) throws Exception;
}
