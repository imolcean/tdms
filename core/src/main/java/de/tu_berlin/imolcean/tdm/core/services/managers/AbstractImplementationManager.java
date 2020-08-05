package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.exceptions.ImplementationNotFoundException;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import de.tu_berlin.imolcean.tdm.core.PublicInterfaceImplementationManager;
import lombok.extern.java.Log;
import org.pf4j.spring.SpringPluginManager;

import java.util.List;
import java.util.Optional;

@Log
public abstract class AbstractImplementationManager<T extends PublicInterface> implements PublicInterfaceImplementationManager<T>
{
    private final Class<T> clazz;

    protected final SpringPluginManager plugins;

    protected T selected;

    public AbstractImplementationManager(SpringPluginManager plugins, Class<T> clazz)
    {
        this.plugins = plugins;
        this.clazz = clazz;
        this.selected = null;
    }

    @Override
    public Optional<T> getSelectedImplementation()
    {
        return Optional.ofNullable(selected);
    }

    @Override
    public List<T> getAvailableImplementations()
    {
        return plugins.getExtensions(clazz);
    }

    @Override
    public void selectImplementation(String implClassName)
    {
        List<T> implementations = getAvailableImplementations();

        selected = implementations.stream()
                .filter(implementation -> implementation.getClass().getName().equalsIgnoreCase(implClassName))
                .findFirst()
                .orElseThrow(() -> new ImplementationNotFoundException(clazz, implClassName));

        log.fine(String.format("Selected %s changed to %s", clazz.getSimpleName(), implClassName));
    }

    @Override
    public void clearSelection()
    {
        selected = null;

        log.fine(String.format("Selection of %s cleared", clazz.getSimpleName()));
    }
}
