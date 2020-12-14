package io.github.imolcean.tdms.core.controllers;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.imolcean.tdms.api.dto.ProjectDto;
import io.github.imolcean.tdms.core.services.ProjectService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

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

    @GetMapping("/data-dir")
    public ResponseEntity<String> dataDir()
    {
        return ResponseEntity.ok(projectService.getDataDir().toString());
    }

    @PutMapping("/data-dir")
    public ResponseEntity<String> changeDataDir(@RequestHeader("X-Dir") String dir)
    {
        projectService.changeDataDir(Path.of(dir));

        return ResponseEntity.ok(projectService.getDataDir().toString());
    }

    @GetMapping(value = "/")
    public ResponseEntity<ProjectDto> project()
    {
        return ResponseEntity.ok(projectService.save());
    }

    @GetMapping(value = "/save")
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
    public ResponseEntity<ProjectDto> open(@RequestParam("file") MultipartFile file) throws Exception
    {
        ProjectDto project = mapper.readValue(file.getInputStream(), ProjectDto.class);
        projectService.open(project);

        return ResponseEntity.ok(projectService.save());
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> close()
    {
        projectService.close();

        return ResponseEntity.noContent().build();
    }
}
