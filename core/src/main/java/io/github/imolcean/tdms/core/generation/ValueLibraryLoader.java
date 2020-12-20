package io.github.imolcean.tdms.core.generation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imolcean.tdms.api.ValueLibrary;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@Log
public class ValueLibraryLoader
{
    private final String libDir;

    @Getter
    private final Map<String, ValueLibrary> lists;

    @Getter
    private final Map<String, ValueLibrary> libraries;

    public ValueLibraryLoader(@Value("${app.generation.lib.path}") String libDir) throws IOException
    {
        this.libDir = libDir;
        this.lists = new HashMap<>();
        this.libraries = new HashMap<>();

        load();
    }

    private void load() throws IOException
    {
        Collection<File> libs = FileUtils.listFiles(Path.of(libDir).toFile(), new String[]{"json"}, false);
        ObjectMapper mapper = new ObjectMapper();

        for(File lib : libs)
        {
            JsonNode root = mapper.readTree(lib);

            if(!root.hasNonNull("_id"))
            {
                log.warning(String.format("File %s contains no valid '_id' and will be ignored", lib.getName()));
                continue;
            }

            String id = root.get("_id").textValue();

            ValueLibrary library = mapper.treeToValue(root, ValueLibrary.class);

            log.fine(String.format("Value Library %s found", id));
            libraries.put(id, library);

            if(library.isList())
            {
                log.fine(String.format("%s is a Value List", id));
                lists.put(id, library);
            }
        }
    }
}
