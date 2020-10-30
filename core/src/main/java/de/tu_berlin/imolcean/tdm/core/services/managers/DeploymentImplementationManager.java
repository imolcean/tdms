package de.tu_berlin.imolcean.tdm.core.services.managers;

import de.tu_berlin.imolcean.tdm.api.interfaces.deployer.Deployer;
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
