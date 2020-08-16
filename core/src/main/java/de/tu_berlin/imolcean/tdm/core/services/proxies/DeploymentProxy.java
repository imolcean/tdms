package de.tu_berlin.imolcean.tdm.core.services.proxies;

import de.tu_berlin.imolcean.tdm.api.interfaces.deployment.Deployer;
import de.tu_berlin.imolcean.tdm.core.services.managers.PublicInterfaceImplementationManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class DeploymentProxy extends AbstractPublicInterfaceProxy<Deployer> implements Deployer
{
    public DeploymentProxy(PublicInterfaceImplementationManager<Deployer> manager)
    {
        super(manager, Deployer.class);
    }

    @Override
    public void deploy(DataSource srcDs, DataSource targetDs) throws Exception
    {
        getImplementation().deploy(srcDs, targetDs);
    }
}
