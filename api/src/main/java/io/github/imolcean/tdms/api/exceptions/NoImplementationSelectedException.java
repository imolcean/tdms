package io.github.imolcean.tdms.api.exceptions;

import io.github.imolcean.tdms.api.interfaces.PublicInterface;

public class NoImplementationSelectedException extends RuntimeException
{
    public NoImplementationSelectedException(Class<? extends PublicInterface> api)
    {
        super(String.format("There is no %s selected currently", api.getSimpleName()));
    }
}
