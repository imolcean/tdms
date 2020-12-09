package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.exceptions.DataGenerationException;
import io.github.imolcean.tdms.api.interfaces.generation.method.ColumnAwareGenerationMethod;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
import io.github.imolcean.tdms.api.GenerationMethodParamDescription;
import io.github.imolcean.tdms.api.services.LowLevelDataService;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log
public class FkGenerationMethod implements GenerationMethod, ColumnAwareGenerationMethod
{
    private final LowLevelDataService lowLevelDataService;
    private final Connection connection;
    private final Column column;

    public FkGenerationMethod(LowLevelDataService lowLevelDataService,
                              Connection connection,
                              Column column)
    {
        this.lowLevelDataService = lowLevelDataService;
        this.connection = connection;
        this.column = column;
    }

    public Object pick(Column pkColumn)
    {
        // Get all values of pkColumn and pick one
        List<Object> pks;
        try
        {
            pks = lowLevelDataService.getTableContentForColumn(connection, pkColumn.getParent(), pkColumn);
        }
        catch(SQLException e)
        {
            throw new DataGenerationException(e);
        }

        log.fine(String.format("Picking one of %s PKs", pks.size()));

        int index = new IntegerGenerationMethod().generate(0, pks.size());

        return pks.get(index);
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
