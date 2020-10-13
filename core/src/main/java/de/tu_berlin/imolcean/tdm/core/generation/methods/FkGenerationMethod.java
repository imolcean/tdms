package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// TODO Make generic?

@Log
public class FkGenerationMethod implements GenerationMethod
{
    private final DataSourceWrapper ds;
    private final TableContentService tableContentService;

    public FkGenerationMethod(DataSourceWrapper ds,
                              TableContentService tableContentService)
    {
        this.ds = ds;
        this.tableContentService = tableContentService;
    }

    public Object pick(Column pkColumn, boolean postpone)
    {
        if(postpone)
        {
            // Generate random value based on Column type
            return new DefaultGenerationMethod().generate(pkColumn);
        }
        else
        {
            // Get all values of pkColumn and pick one
            try
            {
                Object[] pks = tableContentService
                        .getTableContentForColumns(
                                ds,
                                pkColumn.getParent(),
                                Collections.singletonList(pkColumn))
                        .get(0);

                int index = new IntegerGenerationMethod().generate(0, pks.length);

                return pks[index];
            }
            catch(SQLException e)
            {
                throw new DataGenerationException(e);
            }
        }
    }

    public Object pick(Column pkColumn)
    {
        return pick(pkColumn, false);
    }

    @Override
    public Object generate(Column column, Map<String, Object> params)
    {
        Column pkColumn = column.getReferencedColumn();

        if(pkColumn == null)
        {
            throw new IllegalStateException(String.format("Cannot use FkGenerationMethod on column %s because it's not part of a foreign key", column.getName()));
        }

        return pick(pkColumn);
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return Collections.emptyList();
    }
}
