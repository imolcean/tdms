package io.github.imolcean.tdms.core.controllers.implementations;

import io.github.imolcean.tdms.api.interfaces.exporter.DataExporter;
import io.github.imolcean.tdms.core.services.managers.DataExportImplementationManager;
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
