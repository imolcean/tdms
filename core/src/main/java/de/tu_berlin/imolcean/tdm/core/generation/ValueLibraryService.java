package de.tu_berlin.imolcean.tdm.core.generation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ValueLibraryService
{
    private final String libDir;

    @Getter
    private List<String> allLibNames;

    @Getter
    private Map<String, List<Object>> lists;

    public ValueLibraryService(@Value("${app.generation.lib}") String libDir) throws IOException
    {
        this.libDir = libDir;
        this.allLibNames = new ArrayList<>();
        this.lists = new HashMap<>();

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
            allLibNames.add(id);

            log.fine(String.format("Value Library %s found", id));

            if(root.hasNonNull("_list"))
            {
                log.fine(String.format("%s is a Value List", id));

                Object[] valueListElements = mapper.treeToValue(root.get("_list"), Object[].class);
                lists.put(id, Arrays.asList(valueListElements));
            }
        }
    }
}
