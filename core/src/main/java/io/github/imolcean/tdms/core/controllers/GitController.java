package io.github.imolcean.tdms.core.controllers;

import io.github.imolcean.tdms.api.dto.GitRepositoryDto;
import io.github.imolcean.tdms.core.controllers.mappers.GitRepositoryMapper;
import io.github.imolcean.tdms.core.services.GitService;
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
