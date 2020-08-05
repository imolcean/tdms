package de.tu_berlin.imolcean.tdm.api.exceptions;

import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;

public class ImplementationNotFoundException extends RuntimeException
{
    public ImplementationNotFoundException(Class<? extends PublicInterface> api, String name)
    {
        super(String.format("No %s found with the name %s", api.getSimpleName(), name));
    }
}
