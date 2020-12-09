package io.github.imolcean.tdms.api.exceptions;

public class GitRepositoryAlreadyOpenException extends RuntimeException
{
    public GitRepositoryAlreadyOpenException()
    {
        super("A repository is already open");
    }
}
