package de.tu_berlin.imolcean.tdm.api.exceptions;

public class GitRepositoryAlreadyOpenException extends RuntimeException
{
    public GitRepositoryAlreadyOpenException()
    {
        super("A repository is already open");
    }
}
