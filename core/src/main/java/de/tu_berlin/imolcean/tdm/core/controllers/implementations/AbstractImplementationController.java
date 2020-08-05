package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoImplementationSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import de.tu_berlin.imolcean.tdm.core.services.managers.PublicInterfaceImplementationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractImplementationController<T extends PublicInterface>
{
    private final Class<T> clazz;

    protected final PublicInterfaceImplementationManager<T> manager;

    public AbstractImplementationController(PublicInterfaceImplementationManager<T> manager, Class<T> clazz)
    {
        this.clazz = clazz;
        this.manager = manager;
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAvailable()
    {
        List<String> names =
                manager.getAvailableImplementations().stream()
                        .map(implementation -> implementation.getClass().getName())
                        .collect(Collectors.toList());

        return ResponseEntity.ok(names);
    }

    @GetMapping("/selected")
    public ResponseEntity<String> getSelected()
    {
        T selected = manager.getSelectedImplementation()
                .orElseThrow(() -> new NoImplementationSelectedException(clazz));

        return ResponseEntity.ok(selected.getClass().getName());
    }

    @PutMapping("/selected/{name}")
    public ResponseEntity<Void> select(@PathVariable("name") String name)
    {
        manager.selectImplementation(name);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/selected")
    public ResponseEntity<Void> clear()
    {
        manager.clearSelection();

        return ResponseEntity.noContent().build();
    }
}
