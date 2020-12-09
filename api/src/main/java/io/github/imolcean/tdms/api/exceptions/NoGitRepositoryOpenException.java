package io.github.imolcean.tdms.api.exceptions;

public class NoGitRepositoryOpenException extends RuntimeException
{
    public NoGitRepositoryOpenException()
    {
        super("No Git repository is currently open");
    }
}
