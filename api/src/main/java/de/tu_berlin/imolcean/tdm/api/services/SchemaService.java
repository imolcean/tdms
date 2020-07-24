package de.tu_berlin.imolcean.tdm.api.services;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public interface SchemaService
{
    Catalog getSchema(DataSource ds) throws SQLException, SchemaCrawlerException;

    List<String> getTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    List<String> getOccupiedTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    List<String> getEmptyTableNames(DataSource ds) throws SQLException, SchemaCrawlerException;

    Table getTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;

    boolean tableExists(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;

    void dropTable(DataSource ds, String tableName) throws SQLException, SchemaCrawlerException;
}
