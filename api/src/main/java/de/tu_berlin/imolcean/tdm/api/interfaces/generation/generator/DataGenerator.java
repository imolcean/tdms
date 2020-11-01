package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

public interface DataGenerator extends PublicInterface, ExtensionPoint
{
    void generate(DataSourceWrapper ds) throws Exception;
}