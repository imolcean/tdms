package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoSchemaUpdaterSelectedException;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.SchemaUpdaterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/schema-updaters")
public class SchemaUpdaterController
{
    SchemaUpdaterService schemaUpdaterService;

    public SchemaUpdaterController(SchemaUpdaterService schemaUpdaterService)
    {
        this.schemaUpdaterService = schemaUpdaterService;
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAvailable()
    {
        List<String> names =
                schemaUpdaterService.getAvailableSchemaUpdaters().stream()
                        .map(updater -> updater.getClass().getName())
                        .collect(Collectors.toList());

        return ResponseEntity.ok(names);
    }

    @GetMapping("/selected")
    public ResponseEntity<String> getSelected()
    {
        SchemaUpdater selected = schemaUpdaterService.getSelectedSchemaUpdater()
                .orElseThrow(NoSchemaUpdaterSelectedException::new);

        return ResponseEntity.ok(selected.getClass().getName());
    }

    @PutMapping("/selected/{name}")
    public ResponseEntity<Void> select(@PathVariable("name") String name)
    {
        schemaUpdaterService.selectSchemaUpdater(name);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/selected")
    public ResponseEntity<Void> clear()
    {
        schemaUpdaterService.clearSelection();

        return ResponseEntity.noContent().build();
    }
}
