package io.github.imolcean.tdms.core.services.proxies;

import io.github.imolcean.tdms.api.interfaces.deployer.Deployer;
import io.github.imolcean.tdms.core.services.managers.PublicInterfaceImplementationManager;
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
