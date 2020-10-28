package de.tu_berlin.imolcean.tdm.core.generation;

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Log

// TODO Test!

public class FormulaFunctionService
{
    private final String funcDir;

    @Getter
    private final Map<String, String> functions;

    public FormulaFunctionService(@Value("${app.generation.func}") String funcDir) throws IOException
    {
        this.funcDir = funcDir;
        this.functions = new HashMap<>();

        load();
    }

    private void load() throws IOException
    {
        Collection<File> files = FileUtils.listFiles(Path.of(funcDir).toFile(), new String[]{"js"}, false);

        for(File file : files)
        {
            functions.put(file.getName().split(".js")[0], Files.readString(file.toPath()));

            log.fine(String.format("Function %s is found", file.getName()));
        }
    }
}
