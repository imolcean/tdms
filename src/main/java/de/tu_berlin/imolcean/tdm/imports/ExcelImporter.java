package de.tu_berlin.imolcean.tdm.imports;

import de.tu_berlin.imolcean.tdm.SchemaExtractor;
import lombok.extern.java.Log;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

@Service
@Log
public class ExcelImporter implements Importer
{
    private final Collection<Table> tables;

    // TODO Remove
    public final Collection<String> filledTables;

    // TODO Use InternalDataSource
    public ExcelImporter(@Qualifier("StageDataSource") DataSource internalDs,
                         SchemaExtractor schemaExtractor) throws Exception
    {
        this.tables = schemaExtractor.extractDboTables(internalDs).getTables();
        this.filledTables = new ArrayList<>();

        // TODO Check that DB is empty?
    }

    @Override
    public void importFile(Path path)
    {
        log.fine("Importing " + path.toString());

        try(Workbook workbook = new XSSFWorkbook(path.toFile()))
        {

            for(int i = 0; i < workbook.getNumberOfSheets(); i++)
            {
                importSheet(workbook.getSheetAt(i));
            }
        }
        catch(IOException e)
        {
            log.severe(String.format("File %s could not be opened. I will ignore it.", path.toString()));
        }
        catch(InvalidFormatException e)
        {
            log.severe(String.format("File %s has a wrong format. I will ignore it.", path.toString()));
        }
        catch(NotOfficeXmlFileException e)
        {
            log.severe(String.format("File %s has raw XML 2003 format (SpreadsheetML), which is not supported. I will ignore it.", path.toString()));
        }
    }

    /**
     * TODO
     *
     * @param sheet
     */
    private void importSheet(Sheet sheet)
    {
        int idx = sheet.getWorkbook().getSheetIndex(sheet);
        String name = sheet.getWorkbook().getSheetName(idx);

        boolean tableWithSheetNameFound = tables.stream()
                .anyMatch(table -> table.getName().equalsIgnoreCase(name));

        if(!tableWithSheetNameFound)
        {
            log.warning(String.format("No corresponding table found for the sheet %s. I will ignore it.", name));
            return;
        }

        log.fine("Importing " + name);

        filledTables.add(name);
    }
}
