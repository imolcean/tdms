package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.interfaces.updater.SchemaUpdater;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/schema-updaters")
public class SchemaUpdateImplementationController extends AbstractImplementationController<SchemaUpdater>
{
    public SchemaUpdateImplementationController(SchemaUpdateImplementationManager manager)
    {
        super(manager, SchemaUpdater.class);
    }
}
