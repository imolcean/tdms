package de.tu_berlin.imolcean.tdm.core.services;

import de.tu_berlin.imolcean.tdm.api.exceptions.NoOpenProjectException;
import lombok.Getter;
import lombok.extern.java.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Log
public class GitService
{
    private Repository repo;
    private CredentialsProvider credentials;

    // TODO
    @Getter
    private String token;

    public GitService()
    {
        repo = null;
        credentials = null;
        token = null;
    }

    public Path getDir()
    {
        if(repo == null)
        {
            throw new NoOpenProjectException();
        }

        return repo.getDirectory().toPath().getParent();
    }

    public String getUrl()
    {
        if(repo == null)
        {
            throw new NoOpenProjectException();
        }

        return repo.getConfig().getString("remote", "origin", "url");
    }

    public void openRepository(String url, Path dir, String token) throws GitAPIException, IOException
    {
        log.info("Opening git repository");
        log.fine("Looking for an existing repository at " + dir.toString());

        this.token = token;
        this.credentials = new UsernamePasswordCredentialsProvider(token, "");

        Path gitDir = Paths.get(dir.toAbsolutePath().toString(), ".git");

        if(!gitDir.toFile().exists())
        {
            log.fine("Repository not found. Cloning " + url);

            Git git = Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(dir.toFile())
                    .setCredentialsProvider(credentials)
                    .call();

            git.close();

            log.fine("Repository cloned");
        }

        repo = new FileRepositoryBuilder()
                .setGitDir(gitDir.toFile())
                .build();

        String currentUrl = repo.getConfig().getString("remote", "origin", "url");

        if(!currentUrl.equalsIgnoreCase(url))
        {
            throw new IllegalStateException(
                    String.format("URL of the currently open repository (%s) mismatches the config (%s)", currentUrl, url));
        }

        log.info("Git repository open");
    }

    @PreDestroy
    public void closeRepository()
    {
        log.info("Closing repository");

        if(repo != null)
        {
            repo.close();
        }

        repo = null;
        credentials = null;
        token = null;

        log.info("Repository closed");
    }

    public void loadData()
    {
        // TODO
    }

    public void saveData()
    {
        // TODO
    }
}
