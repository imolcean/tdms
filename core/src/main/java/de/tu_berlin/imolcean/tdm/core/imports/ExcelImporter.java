package de.tu_berlin.imolcean.tdm.core.imports;

import de.tu_berlin.imolcean.tdm.core.SchemaExtractor;
import lombok.extern.java.Log;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@Service
@Log
public class ExcelImporter
{
    private final DataSource internalDs;

    private final SchemaExtractor schemaExtractor;

    private final Path excelDirPath;

    private Collection<Table> tables;

    // TODO Remove
    public Collection<String> filledTables;

    public ExcelImporter(@Qualifier("InternalDataSource") DataSource internalDs,
                         SchemaExtractor schemaExtractor,
                         @Value("${app.data.excel.path}") String excelDir)
    {
        this.internalDs = internalDs;
        this.schemaExtractor = schemaExtractor;
        this.excelDirPath = Path.of(excelDir);
    }

    /**
     * Tries to import all files from the directory that is set in configuration.
     */
    public void importDirectory() throws Exception
    {
        this.tables = schemaExtractor.extractDboTables(internalDs).getTables();
        this.filledTables = new ArrayList<>();

        // TODO Check that DB is empty?

        try(Stream<Path> paths = Files.walk(excelDirPath))
        {
            paths.forEach(path ->
            {
                if(path.toFile().isFile())
                {
                    return;
                }

                importFile(path);
            });
        }
    }

    /**
     * Tries to import the given file. If the file cannot be imported
     * for some reason, it will be ignored.
     *
     * @param path file that has to be imported
     */
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
     * Imports content of the given Excel {@code sheet} into the table with the same name.
     * If there is no table with the same name, the {@code sheet} is ignored.
     *
     * @param sheet Excel {@link Sheet} to be imported
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
