package de.tu_berlin.imolcean.tdm.x;

import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import liquibase.diff.DiffResult;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class DiffMapper
{
    private final SchemaService schemaService;

    private final DataSource internalDs;
    private final DataSource tmpDs;

    public DiffMapper(SchemaService schemaService, DataSource internalDs, DataSource tmpDs)
    {
        this.schemaService = schemaService;
        this.internalDs = internalDs;
        this.tmpDs = tmpDs;
    }

    public SchemaUpdater.SchemaUpdate toSchemaUpdate(DiffResult diff) throws SQLException, SchemaCrawlerException
    {
        List<String> untouchedTables = schemaService.getTableNames(internalDs);

        List<Table> addedTables = new ArrayList<>();
        List<Table> deletedTables = new ArrayList<>();
        List<SchemaUpdater.SchemaUpdate.Comparison> changedTables = new ArrayList<>();

        // Collecting added tables
        for(liquibase.structure.core.Table obj : diff.getUnexpectedObjects(liquibase.structure.core.Table.class))
        {
            addedTables.add(schemaService.getTable(tmpDs, obj.getName()));
        }

        // Collecting removed tables
        for(liquibase.structure.core.Table obj : diff.getMissingObjects(liquibase.structure.core.Table.class))
        {
            deletedTables.add(schemaService.getTable(internalDs, obj.getName()));
            untouchedTables.remove(obj.getName());
        }

        // Collecting changed tables
        ListIterator<String> it = untouchedTables.listIterator();
        while(it.hasNext())
        {
            String tableName = it.next();

            if(isTableTouched(diff, tableName))
            {
                Table before = schemaService.getTable(internalDs, tableName);
                Table after = schemaService.getTable(tmpDs, tableName);

                changedTables.add(new SchemaUpdater.SchemaUpdate.Comparison(before, after));
                it.remove();
            }
        }

        return new SchemaUpdater.SchemaUpdate(untouchedTables, addedTables, deletedTables, changedTables);
    }

    /**
     * A table is considered touched if it was renamed or its columns or keys were added/removed/changed.
     *
     * TODO Test
     *
     * @param diff difference between the old and the new schema
     * @param tableName name of the table that might be touched
     * @return {@code true} if the table was touched, {@code false} otherwise
     */
    private boolean isTableTouched(DiffResult diff, String tableName)
    {
        boolean tableChanged = diff.getChangedObjects(liquibase.structure.core.Table.class).keySet().stream()
                .map(Relation::getName)
                .anyMatch(name -> name.equals(tableName));

        boolean hasAdddedColumns = diff.getUnexpectedObjects(Column.class).stream()
                .map(column -> column.getRelation().getName())
                .anyMatch(name -> name.equals(tableName));

        boolean hasMissingColumns = diff.getMissingObjects(liquibase.structure.core.Column.class).stream()
                .map(column -> column.getRelation().getName())
                .anyMatch(name -> name.equals(tableName));

        boolean hasChangedColumns = diff.getChangedObjects(liquibase.structure.core.Column.class).keySet().stream()
                .map(column -> column.getRelation().getName())
                .anyMatch(name -> name.equals(tableName));

        // TODO Check column attributes
        // TODO Check PK
        // TODO Check FKs

        return tableChanged || hasAdddedColumns || hasMissingColumns || hasChangedColumns;
    }
}
