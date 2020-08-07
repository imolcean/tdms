package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.interfaces.exporter.DataExporter;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataExportImplementationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/data-exporters")
public class DataExportImplementationController extends AbstractImplementationController<DataExporter>
{
    public DataExportImplementationController(DataExportImplementationManager manager)
    {
        super(manager, DataExporter.class);
    }
}
