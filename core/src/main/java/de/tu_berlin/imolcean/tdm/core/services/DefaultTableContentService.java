package de.tu_berlin.imolcean.tdm.core.services;

import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.api.exceptions.IllegalSizeOfTableContentRowException;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableContentRowIndexOutOfBoundsException;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.TableContentResultSetHandler;
import de.tu_berlin.imolcean.tdm.core.utils.QueryLoader;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultTableContentService implements TableContentService
{
    // TODO Receive XyzRequest instead of Object[]
    // TODO DRY?
    // TODO Model TableContentRow

    @Override
    public int getTableRowCount(DataSource ds, Table table) throws SQLException
    {
        log.info("Retrieving row count of table " + table.getName());

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table.getName());

            rs.next();

            log.info("Row count retrieved");

            return rs.getInt(1);
        }
    }

    @Override
    public List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException
    {
        log.info("Retrieving content of the table " + table.getName());

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            log.info("Content retrieved");

            return new TableContentResultSetHandler().handle(rs);
        }
    }

    // TODO Return row
    @Override
    public void insertRow(DataSource ds, Table table, Object[] row) throws SQLException
    {
        log.info("Inserting a new row into table " + table.getName());

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            assert table.getColumns().size() == rs.getMetaData().getColumnCount();

            if(row.length != table.getColumns().size())
            {
                throw new IllegalSizeOfTableContentRowException(table.getName(), table.getColumns().size(), row.length);
            }

            rs.moveToInsertRow();

            for(int i = 0; i < table.getColumns().size(); i++)
            {
                rs.updateObject(i + 1, row[i]);
            }

            rs.insertRow();
        }

        log.info("Row inserted");
    }

    @Override
    public void insertRows(DataSource ds, Table table, List<Object[]> rows) throws SQLException
    {
        try(Connection connection = ds.getConnection())
        {
            connection.setAutoCommit(false);

            try
            {
                insertRows(ds.getConnection(), table, rows);
            }
            catch(SQLException e)
            {
                log.warning("Insertion failed");

                connection.rollback();
                throw e;
            }

            connection.commit();
        }
    }

    // TODO Return row
    @Override
    public void updateRow(DataSource ds, Table table, int rowIndex, Object[] row) throws SQLException
    {
        log.info(String.format("Updating the row nr. %s in table %s", rowIndex, table.getName()));

        int rowCount = getTableRowCount(ds, table);

        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new TableContentRowIndexOutOfBoundsException(table.getName(), rowCount, rowIndex);
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            assert table.getColumns().size() == rs.getMetaData().getColumnCount();

            if(row.length != table.getColumns().size())
            {
                throw new IllegalSizeOfTableContentRowException(table.getName(), table.getColumns().size(), row.length);
            }

            int currentRowIndex = 0;
            while(rs.next())
            {
                if(currentRowIndex == rowIndex)
                {
                    for(int i = 0; i < table.getColumns().size(); i++)
                    {
                        rs.updateObject(i + 1, row[i]);
                    }

                    rs.updateRow();
                    break;
                }

                currentRowIndex++;
            }
        }

        log.info("Row updated");
    }

    @Override
    public void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException
    {
        log.info(String.format("Deleting the row nr. %s from table %s", rowIndex, table.getName()));

        int rowCount = getTableRowCount(ds, table);

        if(rowCount <= rowIndex || rowIndex < 0)
        {
            throw new TableContentRowIndexOutOfBoundsException(table.getName(), rowCount, rowIndex);
        }

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            int currentRowIndex = 0;
            while(rs.next())
            {
                if(currentRowIndex == rowIndex)
                {
                    rs.deleteRow();
                    break;
                }

                currentRowIndex++;
            }
        }

        log.info("Row deleted");
    }

    @Override
    public void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException, IOException
    {
        log.info("Copying all data");

        try(Connection targetConnection = target.getConnection())
        {
            log.fine("Disabling database constraints");
            try(Statement statement = targetConnection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("disable_constraints"));
            }

            targetConnection.setAutoCommit(false);

            for(Table table : tables)
            {
                List<Object[]> data = getTableContent(src, table);

                try
                {
                    insertRows(targetConnection, table, data);
                }
                catch(SQLException e)
                {
                    log.warning("Copying of data failed");

                    targetConnection.rollback();
                    throw e;
                }
            }

            targetConnection.commit();
            targetConnection.setAutoCommit(true);

            log.fine("Enabling database constraints");
            try(Statement statement = targetConnection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("enable_constraints"));
            }
        }

        log.info("All data copied");
    }

    @Override
    public boolean isTableEmpty(DataSource ds, Table table) throws SQLException
    {
        return getTableRowCount(ds, table) == 0;
    }

    @Override
    public boolean areTablesEmpty(DataSource ds, Collection<Table> tables) throws SQLException
    {
        for(Table table : tables)
        {
            if(!isTableEmpty(ds, table))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void clearTable(DataSource ds, Table table) throws SQLException
    {
        log.info("Clearing table " + table.getName());

        try(Connection connection = ds.getConnection())
        {
            connection.setAutoCommit(false);

            try
            {
                clearTable(connection, table);
            }
            catch(SQLException e)
            {
                connection.rollback();
                throw e;
            }

            connection.commit();
        }

        log.info("Table cleared");
    }

    @Override
    public void clearTables(DataSource ds, Collection<Table> tables) throws SQLException, IOException
    {
        log.info("Clearing all tables");

        try(Connection connection = ds.getConnection())
        {
            log.fine("Disabling database constraints");
            try(Statement statement = connection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("disable_constraints"));
            }

            connection.setAutoCommit(false);

            try
            {
                for(Table table : tables)
                {
                    log.fine("Clearing table " + table.getName());

                    clearTable(connection, table);
                }
            }
            catch(SQLException e)
            {
                log.warning("Clearing all tables failed");

                connection.rollback();
                throw e;
            }

            connection.commit();
            connection.setAutoCommit(true);

            log.fine("Enabling database constraints");
            try(Statement statement = connection.createStatement())
            {
                statement.execute(QueryLoader.loadQuery("enable_constraints"));
            }
        }

        log.info("All tables cleared");
    }

    private void insertRows(Connection connection, Table table, List<Object[]> rows) throws SQLException
    {
        log.info("Inserting multiple new rows into table " + table.getName());

        List<String> columnNames = table.getColumns().stream()
                .map(NamedObject::getName)
                .collect(Collectors.toList());

        List<JavaSqlType> columnTypes = table.getColumns().stream()
                .map(col -> col.getColumnDataType().getJavaSqlType())
                .collect(Collectors.toList());

        String placeholders = new StringBuilder()
                .append("?,".repeat(columnNames.size()))
                .reverse()
                .deleteCharAt(0)
                .toString();

        String insertSql = String
                .format("INSERT INTO %s (%s) VALUES (%s)",
                        table.getName(),
                        Strings.join(", ", columnNames),
                        placeholders);

        log.fine("Request template: " + insertSql);

        try(PreparedStatement statement = connection.prepareStatement(insertSql))
        {
            for(Object[] row : rows)
            {
                statement.clearParameters();

                int i = 0;
                for(JavaSqlType type : columnTypes)
                {
                    statement.setObject(i + 1, row[i], type);
                    i++;
                }

                statement.addBatch();
            }

            statement.executeBatch();
        }

        log.info("Rows inserted");
    }

    private void clearTable(Connection connection, Table table) throws SQLException
    {
        try(Statement statement = connection.createStatement())
        {
            //noinspection SqlWithoutWhere
            statement.executeUpdate("DELETE FROM " + table.getName());
        }
    }

//    @Override
//    public int countTableContentRowReferences(DataSource ds, Table table, Object[] row)
//    {
//        log.info("Looking for rows in other tables that reference this row through FKs");
//
//        int cnt = 0;
//
//        for(ForeignKey fk : table.getExportedForeignKeys())
//        {
//            String fkTable = null;
//            List<String> whereClauseParts = new ArrayList<>();
//
//            for(ForeignKeyColumnReference ref : fk.getColumnReferences())
//            {
//                Column pkCol = ref.getPrimaryKeyColumn();
//                Column fkCol = ref.getForeignKeyColumn();
//
//                if(Strings.isEmpty(fkTable))
//                {
//                    fkTable = fkCol.getParent().getName();
//                }
//
//                assert fkTable.equalsIgnoreCase(fkCol.getParent().getName());
//
//                int pkColumnIndex = TableContentUtils.getColumnIndex(table, pkCol.getName());
//                String pkValue = row[pkColumnIndex].toString();
//
//                whereClauseParts.add(String.format("%s = %s", fkCol.getName(), pkValue));
//            }
//
//            String req = String.format("SELECT COUNT(*) FROM %s WHERE %s",
//                    fkTable,
//                    String.join(", ", whereClauseParts));
//
//            log.fine(req);
//
//            int cntFk = new JdbcTemplate(ds).queryForObject(req, Integer.class);
//
//            log.fine(String.format("For FK %s, found %s rows that reference this row", fk.getName(), cntFk));
//
//            cnt += cntFk;
//        }
//
//        log.info(String.format("In total, %s rows found referencing this row", cnt));
//
//        return cnt;
//    }
}
