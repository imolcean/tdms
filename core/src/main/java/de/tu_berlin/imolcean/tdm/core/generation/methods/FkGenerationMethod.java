package de.tu_berlin.imolcean.tdm.core.generation.methods;

import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;
import de.tu_berlin.imolcean.tdm.api.TableContent;
import de.tu_berlin.imolcean.tdm.api.exceptions.DataGenerationException;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.generation.GenerationMethodParamDescription;
import lombok.extern.java.Log;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO Implement ColumnDependantGenerationMethod
@Log
public class FkGenerationMethod implements GenerationMethod
{
    private final DataSourceWrapper ds;
//    private final TableContentService tableContentService;
    private final Map<Table, TableContent> generated;
    private final Column column;

    public FkGenerationMethod(DataSourceWrapper ds,
//                              TableContentService tableContentService,
                              Map<Table, TableContent> generated,
                              Column column)
    {
        this.ds = ds;
//        this.tableContentService = tableContentService;
        this.generated = generated;
        this.column = column;
    }

//    public Object pick(Column pkColumn)
//    {
//        // Get all values of pkColumn and pick one
//        try
//        {
//            Object[] pks = tableContentService
//                    .getTableContentForColumns(
//                            ds,
//                            pkColumn.getParent(),
//                            Collections.singletonList(pkColumn))
//                    .get(0);
//
//            int index = new IntegerGenerationMethod().generate(0, pks.length);
//
//            return pks[index];
//        }
//        catch(SQLException e)
//        {
//            throw new DataGenerationException(e);
//        }
//    }

    public Object pick(Column pkColumn)
    {
        // Get all values of pkColumn and pick one
        Object[] pks = generated.get(pkColumn.getParent())
                .getRowsAsMaps().stream()
                .map(row -> row.get(pkColumn))
                .toArray();

        log.fine(String.format("Picking one of %s PKs", pks.length));

        int index = new IntegerGenerationMethod().generate(0, pks.length);

        return pks[index];
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
