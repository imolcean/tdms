package de.tu_berlin.imolcean.tdm.api.exceptions;

public class NoGitRepositoryOpenException extends RuntimeException
{
    public NoGitRepositoryOpenException()
    {
        super("No Git repository is currently open");
    }
}
