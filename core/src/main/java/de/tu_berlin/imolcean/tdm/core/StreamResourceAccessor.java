package de.tu_berlin.imolcean.tdm.core;

import liquibase.resource.AbstractResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class StreamResourceAccessor extends AbstractResourceAccessor
{
    private final String str;

    public StreamResourceAccessor(String str)
    {
        super();
        this.str = str;
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path)
    {
        InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

        Set<InputStream> returnSet = new HashSet<>();
        returnSet.add(stream);
        return returnSet;
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive)
    {
        return null;
    }

    @Override
    public ClassLoader toClassLoader()
    {
        return null;
    }
}
