package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoImplementationSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import de.tu_berlin.imolcean.tdm.core.services.managers.PublicInterfaceImplementationManager;

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
