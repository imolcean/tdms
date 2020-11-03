package de.tu_berlin.imolcean.tdm.core.controllers.implementations;

import de.tu_berlin.imolcean.tdm.api.interfaces.generation.generator.DataGenerator;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataGenerationImplementationManager;
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
