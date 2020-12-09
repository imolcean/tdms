package io.github.imolcean.tdms.core.controllers.implementations;

import io.github.imolcean.tdms.api.interfaces.deployer.Deployer;
import io.github.imolcean.tdms.core.services.managers.PublicInterfaceImplementationManager;
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
