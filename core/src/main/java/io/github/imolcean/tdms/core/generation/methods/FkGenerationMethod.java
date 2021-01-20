package io.github.imolcean.tdms.core.generation.methods;

import io.github.imolcean.tdms.api.exceptions.DataGenerationException;
import io.github.imolcean.tdms.api.interfaces.generation.method.ColumnAwareGenerationMethod;
import io.github.imolcean.tdms.api.interfaces.generation.method.GenerationMethod;
import io.github.imolcean.tdms.api.GenerationMethodParamDescription;
import io.github.imolcean.tdms.api.services.LowLevelDataService;
import io.github.imolcean.tdms.core.generation.ValuePool;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import schemacrawler.schema.Column;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Log
public class FkGenerationMethod implements GenerationMethod, ColumnAwareGenerationMethod
{
    private final LowLevelDataService lowLevelDataService;
    private final Connection connection;
    private final Column column;
    private final Column pkColumn;
    private final boolean unique;

    public FkGenerationMethod(LowLevelDataService lowLevelDataService,
                              Connection connection,
                              Column column,
                              boolean unique)
    {
        this.lowLevelDataService = lowLevelDataService;
        this.connection = connection;
        this.column = column;
        this.pkColumn = column.getReferencedColumn();
        this.unique = unique;

        this.initValuePool();
    }

    private void initValuePool()
    {
        log.fine("Init value pool for FkColumnRule on " + column.getName());

        if(!ValuePool.getPool().containsKey(pkColumn))
        {
            ValuePool.getPool().put(pkColumn, new HashMap<>());
        }

        List<Object> pks;
        List<Object> fks;

        try
        {
            pks = lowLevelDataService.getTableContentForColumn(connection, pkColumn.getParent(), pkColumn);
            fks = lowLevelDataService.getTableContentForColumn(connection, column.getParent(), column);
        }
        catch(SQLException e)
        {
            throw new DataGenerationException(e);
        }

        if(!unique)
        {
            ValuePool.getPool().get(pkColumn).put(column, pks);

            log.fine(String.format("Not unique: pool contains %s values", pks.size()));
        }
        else
        {
            ValuePool.getPool().get(pkColumn).put(column, new ArrayList<>(CollectionUtils.disjunction(pks, fks)));

            log.fine(String.format("Unique: pool contains %s - %s = %s values", pks.size(), fks.size(), ValuePool.getPool().get(pkColumn).get(column).size()));
        }
    }

    public Object pick()
    {
        if(pkColumn == null)
        {
            throw new IllegalStateException(String.format("Cannot use FkGenerationMethod on column %s because it's not part of a foreign key", column.getFullName()));
        }

        List<Object> pks = ValuePool.getPool().get(pkColumn).get(column);

        if(pks.size() == 0)
        {
            throw new DataGenerationException(String.format("Cannot generate unique value for column '%s'", column.getName()));
        }

        log.fine(String.format("Picking one of %s PKs", pks.size()));

        int index = new IntegerGenerationMethod().generate(0, pks.size());

        Object value = pks.get(index);

        if(unique)
        {
            ValuePool.getPool().get(pkColumn).get(column).remove(value);
        }

        return value;
    }

    @Override
    public Object generate(Map<String, Object> params)
    {
        return pick();
    }

    @Override
    public List<GenerationMethodParamDescription> getParamDescription()
    {
        return Collections.emptyList();
    }
}
