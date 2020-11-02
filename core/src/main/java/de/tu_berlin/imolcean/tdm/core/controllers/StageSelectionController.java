package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoCurrentStageException;
import de.tu_berlin.imolcean.tdm.core.StageSelectionContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/stage/")
public class StageSelectionController
{
    @GetMapping("/current")
    public ResponseEntity<String> getSelection()
    {
        String stageName = StageSelectionContextHolder.getStageName();

        if(stageName == null)
        {
            throw new NoCurrentStageException();
        }

        return ResponseEntity.ok(stageName);
    }

    @PutMapping("/current/{name}")
    public ResponseEntity<Void> select(@PathVariable("name") String stageName)
    {
        StageSelectionContextHolder.setStageName(stageName);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> clearSelection()
    {
        StageSelectionContextHolder.clearStageName();

        return ResponseEntity.noContent().build();
    }
}
