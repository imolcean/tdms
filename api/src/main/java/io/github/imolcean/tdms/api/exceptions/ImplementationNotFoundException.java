package io.github.imolcean.tdms.api.exceptions;

import io.github.imolcean.tdms.api.interfaces.PublicInterface;

public class ImplementationNotFoundException extends RuntimeException
{
    public ImplementationNotFoundException(Class<? extends PublicInterface> api, String name)
    {
        super(String.format("No %s found with the name %s", api.getSimpleName(), name));
    }
}
