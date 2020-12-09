package io.github.imolcean.tdms.core.controllers.implementations;

import io.github.imolcean.tdms.api.interfaces.updater.SchemaUpdater;
import io.github.imolcean.tdms.core.services.managers.SchemaUpdateImplementationManager;
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
