package io.github.imolcean.tdms.core.controllers;

import io.github.imolcean.tdms.core.services.DataSourceService;
import io.github.imolcean.tdms.core.services.proxies.DeploymentProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("api/deployment")
public class DeploymentController
{
    private final DataSourceService dsService;
    private final DeploymentProxy deploymentProxy;

    public DeploymentController(DataSourceService dsService, DeploymentProxy deploymentProxy)
    {
        this.dsService = dsService;
        this.deploymentProxy = deploymentProxy;
    }

    @PutMapping("/current")
    public ResponseEntity<Void> deploy() throws Exception
    {
        DataSource internal = dsService.getInternalDataSource();
        DataSource current = dsService.getCurrentStageDataSource();

        deploymentProxy.deploy(internal, current);

        return ResponseEntity.noContent().build();
    }
}
