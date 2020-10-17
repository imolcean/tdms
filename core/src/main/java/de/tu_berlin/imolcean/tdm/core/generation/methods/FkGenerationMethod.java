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

@Log
public class FkGenerationMethod implements GenerationMethod
{
    private final DataSourceWrapper ds;
    private final TableContentService tableContentService;
    private final Column column;

    public FkGenerationMethod(DataSourceWrapper ds,
                              TableContentService tableContentService,
                              Column column)
    {
        this.ds = ds;
        this.tableContentService = tableContentService;
        this.column = column;
    }

    public Object pick(Column pkColumn)
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

    @Override
    public Object generate(Map<String, Object> params)
    {
        Column pkColumn = column.getReferencedColumn();

        if(pkColumn == null)
        {
            throw new IllegalStateException(String.format("Cannot use FkGenerationMethod on column %s because it's not part of a foreign key", column.getFullName()));
        }

        return pick(pkColumn);
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return Collections.emptyList();
    }
}
