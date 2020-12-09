package io.github.imolcean.tdms.core.services.proxies;

import io.github.imolcean.tdms.api.exceptions.NoImplementationSelectedException;
import io.github.imolcean.tdms.api.interfaces.PublicInterface;
import io.github.imolcean.tdms.core.services.managers.PublicInterfaceImplementationManager;

public abstract class AbstractPublicInterfaceProxy<T extends PublicInterface>
{
    private final Class<T> clazz;

    protected final PublicInterfaceImplementationManager<T> manager;

    public AbstractPublicInterfaceProxy(PublicInterfaceImplementationManager<T> manager, Class<T> clazz)
    {
        this.clazz = clazz;
        this.manager = manager;
    }

    protected T getImplementation()
    {
        return manager
                .getSelectedImplementation()
                .orElseThrow(() -> new NoImplementationSelectedException(clazz));
    }
}
