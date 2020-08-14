package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.api.dto.GitRepositoryDto;
import de.tu_berlin.imolcean.tdm.core.controllers.mappers.GitRepositoryMapper;
import de.tu_berlin.imolcean.tdm.core.services.GitService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("api/git")
public class GitController
{
    private final GitService gitService;

    public GitController(GitService gitService)
    {
        this.gitService = gitService;
    }

    @GetMapping("/")
    public ResponseEntity<GitRepositoryDto> getRepository()
    {
        return ResponseEntity.ok(
                GitRepositoryMapper.toDto(gitService));
    }

    @PostMapping("/")
    public ResponseEntity<Void> openRepository(@RequestBody GitRepositoryDto dto) throws GitAPIException, IOException
    {
        gitService.openRepository(dto.getUrl(), Path.of(dto.getDir()), dto.getToken());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> closeRepository()
    {
        gitService.closeRepository();

        return ResponseEntity.noContent().build();
    }
}
