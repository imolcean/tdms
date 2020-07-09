package de.tu_berlin.imolcean.tdm.x.importers;

import de.tu_berlin.imolcean.tdm.api.plugins.SchemaAwareImporter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pf4j.Extension;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

@Extension
@Log
public class ExcelImporter implements SchemaAwareImporter
{
    @Override
    public void importPath(Path path, DataSource ds, Collection<Table> tables) throws IOException, SQLException
    {
        log.info("Importing " + path.toString());

        int sheetsImported;

        try(Connection connection = ds.getConnection())
        {
            if(path.toFile().isDirectory())
            {
                sheetsImported = importDirectory(path, connection, tables);
            }
            else
            {
                sheetsImported = importFile(path, connection, tables);
            }
        }
        catch(InvalidFormatException e)
        {
            throw new IOException(e);
        }

        // TODO Transaction: Import everything or nothing

        log.info(String.format("Import finished. %s tables affected.", sheetsImported));
    }

    private int importDirectory(Path excelDirPath, Connection db, Collection<Table> tables) throws IOException, InvalidFormatException
    {
        log.fine("Importing " + excelDirPath.toString());

        // TODO Check that DB is empty?

        int importedSheets = 0;

        Iterator<File> it = FileUtils.iterateFiles(excelDirPath.toFile(), null, true);
        while(it.hasNext())
        {
            importedSheets += importFile(it.next().toPath(), db, tables);
        }

        return importedSheets;
    }

    private int importFile(Path path, Connection db, Collection<Table> tables) throws IOException, InvalidFormatException
    {
        log.fine("Importing " + path.toString());

        int importedSheets = 0;

        try(Workbook workbook = new XSSFWorkbook(path.toFile()))
        {
            for(int i = 0; i < workbook.getNumberOfSheets(); i++)
            {
                if(importSheet(workbook.getSheetAt(i), db, tables))
                {
                    importedSheets++;
                }
            }
        }

        return importedSheets;
    }

    /**
     * Imports content of the given Excel {@code sheet} into the table with the same name.
     * If there is no table with the same name, the {@code sheet} is ignored.
     *
     * @param sheet Excel {@link Sheet} to be imported
     * @param db {@link Connection} to the database that accepts imported data
     * @param tables {@link Collection} of {@link Table}s that the database has
     * @return {@code true} if the {@code sheet} was imported successfully, {@code false} otherwise
     */
    private boolean importSheet(Sheet sheet, Connection db, Collection<Table> tables)
    {
        int idx = sheet.getWorkbook().getSheetIndex(sheet);
        String name = sheet.getWorkbook().getSheetName(idx);

        boolean tableWithSheetNameFound = tables.stream()
                .anyMatch(table -> table.getName().equalsIgnoreCase(name));

        if(!tableWithSheetNameFound)
        {
            log.warning(String.format("No corresponding table found for the sheet %s. I will ignore it.", name));
            return false;
        }

        log.fine("Importing " + name);

        // TODO Check that columns are the same
        // TODO Import

        return true;
    }
}
