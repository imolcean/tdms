package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.interfaces.deployment.Deployer;
import de.tu_berlin.imolcean.tdm.core.services.managers.PublicInterfaceImplementationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/deployers")
public class DeploymentImplementationController extends AbstractImplementationController<Deployer>
{
    public DeploymentImplementationController(PublicInterfaceImplementationManager<Deployer> manager)
    {
        super(manager, Deployer.class);
    }
}
