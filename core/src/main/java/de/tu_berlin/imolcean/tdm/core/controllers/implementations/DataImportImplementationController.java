package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.interfaces.importer.DataImporter;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/data-importers")
public class DataImportImplementationController extends AbstractImplementationController<DataImporter>
{
    public DataImportImplementationController(DataImportImplementationManager manager)
    {
        super(manager, DataImporter.class);
    }
}
