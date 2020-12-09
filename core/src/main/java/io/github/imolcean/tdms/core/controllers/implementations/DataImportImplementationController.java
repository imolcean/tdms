package io.github.imolcean.tdms.core.controllers.implementations;

import io.github.imolcean.tdms.api.interfaces.importer.DataImporter;
import io.github.imolcean.tdms.core.services.managers.DataImportImplementationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/data-importers")
public class DataImportImplementationController extends AbstractImplementationController<DataImporter>
{
    public DataImportImplementationController(DataImportImplementationManager manager)
    {
        super(manager, DataImporter.class);
    }
}
