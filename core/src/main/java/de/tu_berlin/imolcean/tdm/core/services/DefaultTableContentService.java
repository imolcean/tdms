package de.tu_berlin.imolcean.tdm.core.services;

import de.danielbechler.util.Strings;
import de.tu_berlin.imolcean.tdm.api.exceptions.IllegalSizeOfTableContentRowException;
import de.tu_berlin.imolcean.tdm.api.exceptions.TableContentRowIndexOutOfBoundsException;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import de.tu_berlin.imolcean.tdm.core.TableContentResultSetHandler;
import de.tu_berlin.imolcean.tdm.core.utils.TableContentUtils;
import lombok.extern.java.Log;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log
public class DefaultTableContentService implements TableContentService
{
    // TODO Log end of operation
    // TODO Receive XyzRequest instead of Object[]
    // TODO DRY?
    // TODO Model TableContentRow

    @Override
    public int getTableRowCount(DataSource ds, Table table) throws SQLException
    {
        log.fine("Looking for row count of table " + table.getName());

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table.getName());

            rs.next();

            return rs.getInt(1);
        }
    }

    @Override
    public List<Object[]> getTableContent(DataSource ds, Table table) throws SQLException
    {
        log.fine("Retrieving content of the table " + table.getName());

        try(Connection connection = ds.getConnection(); Statement statement = connection.createStatement())
        {
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table.getName());

            return new TableContentResultSetHandler().handle(rs);
        }
    }

    // TODO Return TableContentRow
    @Override
    public void insertRow(DataSource ds, Table table, Object[] row) throws SQLException
    {
        // TODO Handle auto increment
        log.fine("Inserting a new row into table " + table.getName());

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

        log.fine("Row inserted");
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

    // TODO Return TableContentRow
    @Override
    public void updateRow(DataSource ds, Table table, int rowIndex, Object[] row) throws SQLException
    {
        log.fine(String.format("Updating the row nr. %s in table %s", rowIndex, table.getName()));

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

        log.fine("Row updated");
    }

    @Override
    public void deleteRow(DataSource ds, Table table, int rowIndex) throws SQLException
    {
        log.fine(String.format("Deleting the row nr. %s from table %s", rowIndex, table.getName()));

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

        log.fine("Row deleted");
    }

    // TODO Transaction
    @Override
    public void copyData(DataSource src, DataSource target, Collection<Table> tables) throws SQLException
    {
        log.fine("Copying all data");

        try(Connection targetConnection = target.getConnection())
        {
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
        }

        log.fine("All data copied");
    }

    @Override
    public void clearTable(DataSource ds, Table table) throws SQLException
    {
        log.fine("Deleting all rows from table " + table.getName());

        try(Connection connection = ds.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE))
        {
            connection.setAutoCommit(false);

            try
            {
                //noinspection SqlWithoutWhere
                statement.executeUpdate("DELETE FROM " + table.getName());
            }
            catch(SQLException e)
            {
                connection.rollback();
                throw e;
            }

            connection.commit();
        }

        log.fine("Table cleared");
    }

    private void insertRows(Connection connection, Table table, List<Object[]> rows) throws SQLException
    {
        log.fine("Inserting multiple new rows into table " + table.getName());

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

        log.fine("Rows inserted");
    }

    // TODO
//    @Override
//    public int countTableContentRowReferences(DataSource ds, Table table, Object[] row)
//    {
//        log.fine("Looking for rows in other tables that reference this row through FKs");
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
//        log.fine(String.format("In total, %s rows found referencing this row", cnt));
//
//        return cnt;
//    }
}
