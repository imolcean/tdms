package de.tu_berlin.imolcean.tdm.core.controllers;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("api/project")
public class ProjectController
{
    private final ProjectService projectService;
    private final ObjectMapper mapper;

    public ProjectController(ProjectService projectService)
    {
        this.projectService = projectService;
        this.mapper = new ObjectMapper()
                .setDefaultPrettyPrinter(
                        new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));
    }

    @GetMapping("/name")
    public ResponseEntity<String> name()
    {
        return ResponseEntity.ok(projectService.getProjectName());
    }

    @PutMapping("/name/{name}")
    public ResponseEntity<String> rename(@PathVariable("name") String name)
    {
        projectService.renameProject(name);

        return ResponseEntity.ok(projectService.getProjectName());
    }

    @GetMapping(value = "/")
    public ResponseEntity<Resource> save() throws IOException
    {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            ProjectDto project = projectService.save();
            mapper.writerWithDefaultPrettyPrinter().writeValue(baos, project);

            try(ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray()))
            {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                String.format("attachment; filename=\"%s.tdm.json\"", projectService.getProjectName()))
                        .body(new InputStreamResource(bais));
            }
        }
    }

    @PostMapping("/")
    public ResponseEntity<Void> open(@RequestParam("file") MultipartFile file) throws Exception
    {
        ProjectDto project = mapper.readValue(file.getInputStream(), ProjectDto.class);
        projectService.open(project);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> close()
    {
        projectService.close();

        return ResponseEntity.noContent().build();
    }
}
