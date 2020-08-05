package de.tu_berlin.imolcean.tdm.api.exceptions;

import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;

public class NoImplementationSelectedException extends RuntimeException
{
    public NoImplementationSelectedException(Class<? extends PublicInterface> api)
    {
        super(String.format("There is no %s selected currently", api.getSimpleName()));
    }
}
