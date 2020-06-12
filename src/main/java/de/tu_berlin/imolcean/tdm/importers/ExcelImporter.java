package de.tu_berlin.imolcean.tdm.importers;

import lombok.extern.java.Log;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
@Log
public class ExcelImporter implements Importer
{
    @Override
    public void importFile(Path path)
    {
        if(path.toFile().isDirectory())
        {
            return;
        }

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

        System.out.println(sheet.getWorkbook().getSheetName(idx));
    }
}
