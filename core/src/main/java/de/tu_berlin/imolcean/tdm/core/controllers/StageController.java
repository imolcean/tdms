package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoCurrentStageException;
import de.tu_berlin.imolcean.tdm.core.StageContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/stage/")
public class StageController
{
    @GetMapping("/current")
    public ResponseEntity<String> getCurrentStageName()
    {
        String stageName = StageContextHolder.getStageName();

        if(stageName == null)
        {
            throw new NoCurrentStageException();
        }

        return ResponseEntity.ok(stageName);
    }

    @PutMapping("/current")
    public ResponseEntity<Void> setCurrentStage(@RequestHeader("TDM-Stage-Name") String stageName)
    {
        StageContextHolder.setStageName(stageName);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/current")
    public ResponseEntity<Void> clearCurrentStageSelection()
    {
        StageContextHolder.clearStageName();

        return ResponseEntity.noContent().build();
    }
}
