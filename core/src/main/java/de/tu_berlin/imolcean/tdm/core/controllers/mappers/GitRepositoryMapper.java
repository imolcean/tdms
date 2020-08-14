package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.GitRepositoryDto;
import de.tu_berlin.imolcean.tdm.core.services.GitService;

public class GitRepositoryMapper
{
    public static GitRepositoryDto toDto(GitService git)
    {
        return new GitRepositoryDto(git.getUrl(), git.getDir().toString(), git.getToken());
    }
}
