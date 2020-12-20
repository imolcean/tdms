package io.github.imolcean.tdms.core.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/paths")
public class PathsController
{
    @Value("${app.path}")
    private String appPath;

    @Value("${app.tmp.path}")
    private String appTmpPath;

    @Value("${app.stages.path}")
    private String appStagesPath;

    @Value("${app.plugins.path}")
    private String appPluginsPath;

    @Value("${app.plugins.config.path}")
    private String appPluginsConfigPath;

    @Value("${app.generation.lib.path}")
    private String appGenerationLibPath;

    @Value("${app.generation.script.path}")
    private String appGenerationScriptPath;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> getPaths()
    {
        Map<String, String> map = new HashMap<>();

        map.put("appPath", this.appPath);
        map.put("appTmpPath", this.appTmpPath);
        map.put("appStagesPath", this.appStagesPath);
        map.put("appPluginsPath", this.appPluginsPath);
        map.put("appPluginsConfigPath", this.appPluginsConfigPath);
        map.put("appGenerationLibPath", this.appGenerationLibPath);
        map.put("appGenerationScriptPath", this.appGenerationScriptPath);

        return ResponseEntity.ok(map);
    }
}
