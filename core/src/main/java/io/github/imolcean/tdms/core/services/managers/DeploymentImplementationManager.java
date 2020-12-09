package io.github.imolcean.tdms.core.services.managers;

import io.github.imolcean.tdms.api.interfaces.deployer.Deployer;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Service;

@Service
public class DeploymentImplementationManager extends AbstractImplementationManager<Deployer>
{
    public DeploymentImplementationManager(SpringPluginManager plugins)
    {
        super(plugins, Deployer.class);
    }
}
