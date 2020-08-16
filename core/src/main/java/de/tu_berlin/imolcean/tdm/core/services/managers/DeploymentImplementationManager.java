package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.deployment.Deployer;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DeploymentImplementationManager extends AbstractImplementationManager<Deployer>
{
    private final SchemaService schemaService;
    private final TableContentService tableContentService;

    public DeploymentImplementationManager(SpringPluginManager plugins,
                                           SchemaService schemaService,
                                           TableContentService tableContentService)
    {
        super(plugins, Deployer.class);

        this.schemaService = schemaService;
        this.tableContentService = tableContentService;
    }

    @Override
    public void selectImplementation(String implClassName)
    {
        super.selectImplementation(implClassName);

        // TODO Replace through DI
        selected.setDependencies(schemaService, tableContentService);
    }
}
