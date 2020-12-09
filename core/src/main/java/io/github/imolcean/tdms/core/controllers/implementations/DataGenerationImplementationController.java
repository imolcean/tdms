package io.github.imolcean.tdms.core.controllers.implementations;

import io.github.imolcean.tdms.api.interfaces.generation.generator.DataGenerator;
import io.github.imolcean.tdms.core.services.managers.DataGenerationImplementationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/data-generators")
public class DataGenerationImplementationController extends AbstractImplementationController<DataGenerator>
{
    public DataGenerationImplementationController(DataGenerationImplementationManager manager)
    {
        super(manager, DataGenerator.class);
    }
}
