package de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.dto.TableRuleDto;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

import java.util.Collection;

public interface DataGenerator extends PublicInterface, ExtensionPoint
{
    void generate(DataSourceWrapper ds, Collection<TableRuleDto> params) throws Exception;
}
