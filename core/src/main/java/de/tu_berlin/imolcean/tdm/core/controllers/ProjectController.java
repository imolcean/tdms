package de.tu_berlin.imolcean.tdm.core.controllers;

import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Properties;

@RestController
@RequestMapping("api/project")
public class ProjectController
{
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService)
    {
        this.projectService = projectService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> open(@RequestParam("file") MultipartFile file) throws IOException
    {
        Properties project = new Properties();
        project.load(file.getInputStream());

        projectService.open(project);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Resource> save() throws IOException
    {
        // TODO File name & top comment according to the project name

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            Properties project = projectService.save();
            project.store(baos, "TDMS Project");

            try(ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray()))
            {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Project.properties\"")
                        .body(new InputStreamResource(bais));
            }
        }
    }
}
