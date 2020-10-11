package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.sql.SQLException;
import java.util.Collections;

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

    public Object pick(Column pkColumn, boolean postpone) throws SQLException
    {
        if(postpone)
        {
            // Generate random value based on Column type

            return new DefaultGenerationMethod().generate(pkColumn);
        }
        else
        {
            // Get all values of pkColumn and pick one

            Object[] pks = tableContentService
                    .getTableContentForColumns(
                            ds,
                            pkColumn.getParent(),
                            Collections.singletonList(pkColumn))
                    .get(0);

            int index = new IntegerGenerationMethod().generate(0, pks.length);

            return pks[index];
        }
    }

    public Object pick(Column pkColumn) throws SQLException
    {
        return pick(pkColumn, false);
    }
}
