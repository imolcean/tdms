package de.tu_berlin.imolcean.tdm.utils;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class QueryLoader
{
    static ResourceLoader resourceLoader;

    static {
        resourceLoader = new DefaultResourceLoader();
    }

    public static String loadQuery(String name) throws IOException
    {
        Resource resource = resourceLoader.getResource("classpath:sql/" + name + ".sql");

        try(Reader reader = new InputStreamReader(resource.getInputStream()))
        {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
