package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.PublicInterface;

import java.util.List;
import java.util.Optional;

public interface PublicInterfaceImplementationManager<T extends PublicInterface>
{
    /**
     * This method is used to retrieve implementation that is currently selected.
     *
     * @return currently selected implementation of the specified {@link PublicInterface}
     *         or an empty {@link Optional} if there is no implementation selected currently
     */
    Optional<T> getSelectedImplementation();

    /**
     * This method is used to retrieve all available implementations.
     *
     * @return all implementations of the specified {@link PublicInterface} that are currently available for selection
     */
    List<T> getAvailableImplementations();

    /**
     * Selects one of the available {@link PublicInterface} implementations to be used.
     *
     * @param className fully qualified class name of the {@link PublicInterface} implementation class
     */
    void selectImplementation(String className);

    /**
     * Clears the selection that is made using {@code selectImplementation} method.
     */
    void clearSelection();
}
