package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/schema-updaters")
public class SchemaUpdateImplementationController
{
    SchemaUpdateImplementationManager schemaUpdateImplementationManager;

    public SchemaUpdateImplementationController(SchemaUpdateImplementationManager schemaUpdateImplementationManager)
    {
        this.schemaUpdateImplementationManager = schemaUpdateImplementationManager;
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAvailable()
    {
        List<String> names =
                schemaUpdateImplementationManager.getAvailableImplementations().stream()
                        .map(updater -> updater.getClass().getName())
                        .collect(Collectors.toList());

        return ResponseEntity.ok(names);
    }

    @GetMapping("/selected")
    public ResponseEntity<String> getSelected()
    {
        SchemaUpdater selected = schemaUpdateImplementationManager.getSelectedImplementation()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        return ResponseEntity.ok(selected.getClass().getName());
    }

    @PutMapping("/selected/{name}")
    public ResponseEntity<Void> select(@PathVariable("name") String name)
    {
        schemaUpdateImplementationManager.selectImplementation(name);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/selected")
    public ResponseEntity<Void> clear()
    {
        schemaUpdateImplementationManager.clearSelection();

        return ResponseEntity.noContent().build();
    }
}
