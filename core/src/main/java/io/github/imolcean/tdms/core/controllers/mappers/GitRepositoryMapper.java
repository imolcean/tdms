package io.github.imolcean.tdms.core.controllers.mappers;

import io.github.imolcean.tdms.api.dto.GitRepositoryDto;
import io.github.imolcean.tdms.core.services.GitService;

public class GitRepositoryMapper
{
    public static GitRepositoryDto toDto(GitService git)
    {
        return new GitRepositoryDto(git.getUrl(), git.getDir().toString(), git.getToken());
    }
}
