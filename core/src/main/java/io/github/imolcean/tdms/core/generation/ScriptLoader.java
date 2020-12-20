package io.github.imolcean.tdms.core.generation;

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

public class ScriptLoader
{
    private final String scriptDir;

    @Getter
    private final Map<String, String> scripts;

    public ScriptLoader(@Value("${app.generation.script.path}") String scriptDir) throws IOException
    {
        this.scriptDir = scriptDir;
        this.scripts = new HashMap<>();

        load();
    }

    private void load() throws IOException
    {
        Collection<File> files = FileUtils.listFiles(Path.of(scriptDir).toFile(), new String[]{"js"}, false);

        for(File file : files)
        {
            scripts.put(file.getName().split(".js")[0], Files.readString(file.toPath()));

            log.fine(String.format("Script %s is found", file.getName()));
        }
    }
}
