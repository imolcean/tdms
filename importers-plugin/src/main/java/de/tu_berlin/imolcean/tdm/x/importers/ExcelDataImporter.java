package de.tu_berlin.imolcean.tdm.x.importers;

import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pf4j.Extension;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Extension
@Log
public class ExcelDataImporter implements DataImporter
{
    // TODO Throw DataImportException
    // TODO Transactions

    private final Path excelDir;

    private SchemaService schemaService;
    private TableContentService tableContentService;

    @SuppressWarnings("unused")
    public ExcelDataImporter(Properties properties)
    {
        this.excelDir = Paths.get(properties.getProperty("excel.path"));

        log.fine("Excel import directory: " + excelDir.toString());
    }

    // TODO Insert SchemaService through DI
    @Override
    public void setDependencies(SchemaService schemaService, TableContentService tableContentService)
    {
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void importData(DataSource ds) throws Exception
    {
        Collection<Table> tables = schemaService.getSchema(ds).getTables();

        log.fine("Checking that the database is empty");

        if(!tableContentService.areTablesEmpty(ds, tables))
        {
            throw new Exception("Cannot import into non-empty database");
        }

        log.fine("Database is empty");

        log.info("Importing " + excelDir.toString());

        int sheetsImported;

        if(excelDir.toFile().isDirectory())
        {
            sheetsImported = importDirectory(excelDir, ds, tables);
        }
        else
        {
            sheetsImported = importFile(excelDir, ds, tables);
        }

        log.info(String.format("Import finished. %s tables affected.", sheetsImported));
    }

    private int importDirectory(Path excelDirPath, DataSource ds, Collection<Table> tables) throws IOException, InvalidFormatException, SQLException
    {
        log.fine("Importing " + excelDirPath.toString());

        int importedSheets = 0;

        Iterator<File> it = FileUtils.iterateFiles(excelDirPath.toFile(), null, true);
        while(it.hasNext())
        {
            importedSheets += importFile(it.next().toPath(), ds, tables);
        }

        log.fine(String.format("Import of %s finished", excelDirPath.toString()));

        return importedSheets;
    }

    private int importFile(Path path,DataSource ds, Collection<Table> tables) throws IOException, InvalidFormatException, SQLException
    {
        log.fine("Importing " + path.toString());

        int importedSheets = 0;

        try(Workbook workbook = new XSSFWorkbook(path.toFile()))
        {
            for(int i = 0; i < workbook.getNumberOfSheets(); i++)
            {
                String sheetName = workbook.getSheetName(i);

                try
                {
                    Table table = tables.stream()
                            .filter(t -> t.getName().equals(sheetName))
                            .findFirst()
                            .orElseThrow(() -> new TableNotFoundException(sheetName));

                    importSheet(workbook.getSheetAt(i), ds, table);
                    importedSheets++;
                }
                catch(TableNotFoundException e)
                {
                    log.warning(String.format("No corresponding table found for the sheet %s. I will ignore it.", sheetName));
                }
            }
        }

        log.fine(String.format("Import of %s finished", path.toString()));

        return importedSheets;
    }

    /**
     * Imports content of the given Excel {@code sheet} into the given {@code table}.
     *
     * Import is only made, if the {@code table} has the same columns as the {@code sheet}.
     * The first row of the sheet must contain column names.
     * The second row must contain column data types.
     * The third row must contain nullability info. "NotNull" must be present in columns that are not nullable,
     * empty string should be found otherwise.
     * Rows starting from the fourth are considered data rows, i.e. they contain data to be imported.
     *
     * @param sheet Excel {@link Sheet} to be imported
     * @param ds {@link DataSource} to the database that accepts imported data
     * @param table table that is mapped by the {@code sheet}
     */
    private void importSheet(Sheet sheet, DataSource ds, Table table) throws SQLException
    {
        log.fine("Importing " + table.getName());

        if(!columnNamesMatch(table, sheet))
        {
            log.warning("Column names mismatch. I will ignore the sheet.");
            return;
        }

//        List<Object[]> dbRows = new ArrayList<>();
//
//        for(Row row : sheet)
//        {
//            // First three rows are headers
//            if(row.getRowNum() < 3)
//            {
//                continue;
//            }
//
//            Object[] dbRow = new Object[table.getColumns().size()];
//
//            for(int i = 0; i < table.getColumns().size(); i++)
//            {
//                String val = row.getCell(i).getStringCellValue();
//
//                System.out.printf("%s.%s: %s%n", row.getRowNum(), i, val);
//
//                if(val.equalsIgnoreCase("NULL"))
//                {
//                    val = null;
//                }
//
//                dbRow[i] = val;
//            }
//
//            dbRows.add(dbRow);
//        }
//
////        tableContentService.insertRows(ds, table, dbRows);
//        displayRows(dbRows, table.getName());

        log.fine(String.format("Import of %s finished", table.getName()));
    }

    // TODO Remove
    private void displayRows(List<Object[]> rows, String tableName)
    {
        System.out.println(tableName + ":");

        for(Object[] row : rows)
        {
            for(Object col : row)
            {
                System.out.print(col.toString() + ", ");
            }

            System.out.println();
        }
    }

    private boolean columnNamesMatch(Table table, Sheet sheet)
    {
        List<Column> tableColumns = table.getColumns();
        List<String> sheetColumns = new ArrayList<>();

        try
        {
            for(int i = 0; i < table.getColumns().size(); i++)
            {
                sheetColumns.add(sheet.getRow(0).getCell(i).getStringCellValue());
            }
        }
        catch(NullPointerException e)
        {
            log.warning(String.format("Not enough columns found in the sheet (needed %s but found %s)", tableColumns.size(), sheetColumns.size()));
            return false;
        }

        if(tableColumns.size() != sheetColumns.size())
        {
            log.warning(String.format("Table has %s columns but sheet has %s", tableColumns.size(), sheetColumns.size()));
            return false;
        }

        for(int i = 0; i < tableColumns.size(); i++)
        {
            String tableColumn = tableColumns.get(i).getName();
            String sheetColumn = sheetColumns.get(i);

            if(!tableColumn.equals(sheetColumn))
            {
                log.warning(String.format("Column %s is called '%s' in the table but '%s' in the sheet", i, tableColumn, sheetColumn));
                return false;
            }
        }

        return true;
    }
}
