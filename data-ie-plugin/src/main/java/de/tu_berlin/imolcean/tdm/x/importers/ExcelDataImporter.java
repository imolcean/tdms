package de.tu_berlin.imolcean.tdm.x.importers;

import de.tu_berlin.imolcean.tdm.api.exceptions.TableNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pf4j.Extension;
import schemacrawler.schema.Column;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Extension
@Log
public class ExcelDataImporter implements DataImporter
{
    private SchemaService schemaService;
    private TableContentService tableContentService;

    // TODO Insert SchemaService through DI
    @Override
    public void setDependencies(SchemaService schemaService, TableContentService tableContentService)
    {
        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void importData(DataSource ds, Path importDir) throws Exception
    {
        Collection<Table> tables = schemaService.getSchema(ds).getTables();

        log.fine("Checking that the database is empty");

        if(!tableContentService.areTablesEmpty(ds, tables))
        {
            throw new Exception("Cannot import into non-empty database");
        }

        log.fine("Database is empty");
        log.info("Importing " + importDir.toString());

        if(!importDir.toFile().isDirectory())
        {
            throw new IllegalArgumentException(String.format("%s is not a directory", importDir.toString()));
        }

        Map<Table, List<Object[]>> map = handleDirectory(importDir, tables);

        // TODO Remove
//        displayData(map);

        tableContentService.importData(ds, map);

        log.info(String.format("Import finished. %s tables affected.", map.keySet().size()));
    }

    private Map<Table, List<Object[]>> handleDirectory(Path path, Collection<Table> tables)
            throws IOException, InvalidFormatException
    {
        log.fine("Handling " + path.toString());

        Map<Table, List<Object[]>> map = new HashMap<>();

        Iterator<File> it = FileUtils.iterateFiles(path.toFile(), null, true);
        while(it.hasNext())
        {
            map.putAll(handleFile(it.next().toPath(), tables));
        }

        log.fine(String.format("Finished with %s", path.toString()));

        return map;
    }

    private Map<Table, List<Object[]>> handleFile(Path path, Collection<Table> tables)
            throws IOException, InvalidFormatException
    {
        log.fine("Handling " + path.toString());

        Map<Table, List<Object[]>> map = new HashMap<>();

        try(Workbook workbook = new XSSFWorkbook(path.toFile()))
        {
            for(int i = 0; i < workbook.getNumberOfSheets(); i++)
            {
                Sheet sheet = workbook.getSheetAt(i);

                log.fine(String.format("Handling sheet %s", sheet.getSheetName()));

                try
                {
                    Table table = tables.stream()
                            .filter(t -> t.getName().equals(sheet.getSheetName()))
                            .findFirst()
                            .orElseThrow(() -> new TableNotFoundException(sheet.getSheetName()));

                    if(!columnNamesMatch(table, sheet))
                    {
                        log.warning("Column names mismatch. I will ignore this sheet.");
                        continue;
                    }

                    map.put(table, handleSheet(workbook.getSheetAt(i), table));
                }
                catch(TableNotFoundException e)
                {
                    log.warning(String.format("No corresponding table found for the sheet %s. I will ignore it.", sheet.getSheetName()));
                }
            }
        }

        log.fine(String.format("Finished with %s", path.toString()));

        return map;
    }

    /**
     * Extracts content of the given Excel {@code sheet} that corresponds to the {@code table}.
     *
     * The {@code table} must have the same columns as the {@code sheet}.
     * The first row of the sheet must contain column names.
     * The second row must contain column data types.
     * The third row must contain nullability info. "NotNull" must be present in columns that are not nullable,
     * an empty string should be found otherwise.
     * Rows starting from the fourth are considered data rows, i.e. they contain data to be imported.
     *
     * @param sheet Excel {@link Sheet} to be imported
     * @param table table that is mapped by the {@code sheet}
     * @return List of table rows extracted from the {@code sheet}
     */
    private List<Object[]> handleSheet(Sheet sheet, Table table)
    {
        List<Object[]> dbRows = new ArrayList<>();

        for(Row row : sheet)
        {
            // First three rows are headers
            if(row.getRowNum() < 3)
            {
                continue;
            }

            Object[] dbRow = new Object[table.getColumns().size()];

            for(int i = 0; i < table.getColumns().size(); i++)
            {
                Object val = getCellValue(row.getCell(i));

                // TODO Remove
//                System.out.printf("%s.%s: %s%n", row.getRowNum(), i, val);

                dbRow[i] = val;
            }

            dbRows.add(dbRow);
        }
        
        return dbRows;
    }

    // TODO Remove
    private void displayData(Map<Table, List<Object[]>> data)
    {
        for(Map.Entry<Table, List<Object[]>> entry : data.entrySet())
        {
            System.out.println(entry.getKey().getName() + ":");

            for(Object[] row : entry.getValue())
            {
                for(Object col : row)
                {
                    System.out.print(col + ", ");
                }

                System.out.println();
            }
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

    private Object getCellValue(Cell cell)
    {
        switch(cell.getCellType())
        {
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                log.warning(String.format("Cell %s.%s has an error, I will treat it as a NULL", cell.getRowIndex(), cell.getColumnIndex()));
                return null;
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String str = cell.getStringCellValue();
                if(str.equalsIgnoreCase("NULL"))
                {
                    return null;
                }
                return str;
            case FORMULA:
                log.warning(String.format("Cell %s.%s contains a formula, I will try to evaluate the value", cell.getRowIndex(), cell.getColumnIndex()));
                FormulaEvaluator evaluator =cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                return new DataFormatter().formatCellValue(cell, evaluator);
            default:
                log.warning(String.format("Unknown type of cell %s.%s, I will treat it as a NULL", cell.getRowIndex(), cell.getColumnIndex()));
                return null;
        }
    }
}
