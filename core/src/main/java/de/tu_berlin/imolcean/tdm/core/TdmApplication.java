package de.tu_berlin.imolcean.tdm.core;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tu_berlin.imolcean.tdm.api.dto.ProjectDto;
import de.tu_berlin.imolcean.tdm.core.deployment.MigrationDeployer;
import de.tu_berlin.imolcean.tdm.core.services.DataSourceService;
import de.tu_berlin.imolcean.tdm.core.services.ProjectService;
import de.tu_berlin.imolcean.tdm.core.services.managers.DataImportImplementationManager;
import de.tu_berlin.imolcean.tdm.core.services.managers.SchemaUpdateImplementationManager;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    private DataSourceService dsService;

    @Autowired
    private de.tu_berlin.imolcean.tdm.api.services.SchemaService SchemaService;

    @Autowired
    private SchemaUpdateImplementationManager schemaUpdateImplementationManager;

    @Autowired
    private DataImportImplementationManager dataImportImplementationManager;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MigrationDeployer deployer;

    @Autowired
    private SpringPluginManager plugins;

    private final ObjectMapper mapper = new ObjectMapper()
            .setDefaultPrettyPrinter(
                    new DefaultPrettyPrinter().withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        ProjectDto project = mapper.readValue(new ClassPathResource("DABAG.tdm.json").getInputStream(), ProjectDto.class);
        projectService.open(project);

        StageContextHolder.setStageName("exp");


//        Catalog internalDb = schemaService.getSchema(dsService.getInternalDataSource());
//        Catalog importDb = schemaService.getSchema(dsService.getImportDataSource());
//
//        final DiffNode diff = new SchemaDifferBuilder().build().compare(importDb, internalDb);
//        SchemaDiffPrinter.print(diff);



//        SchemaUpdater liquibase = plugins.getExtensions(SchemaUpdater.class).stream()
//                .findFirst()
//                .orElseThrow();
//
//        liquibase.updateSchema(dsService.getInternalDataSource());


//        deployer.deploy();


//        Table table = schemaService.getTable(internalDs, "PERSON");
//
//        for(ForeignKey fk : table.getExportedForeignKeys())
//        {
//            System.out.println(fk.getFullName());
//
//            for(ForeignKeyColumnReference ref : fk.getColumnReferences())
//            {
//                Column pkCol = ref.getPrimaryKeyColumn();
//                Column fkCol = ref.getForeignKeyColumn();
//
//                System.out.print(fkCol.getParent() + " . " + fkCol.getName());
//                System.out.print("   ->   ");
//                System.out.print(pkCol.getParent() + " . " + pkCol.getName());
//                System.out.println();
//            }
//
//            System.out.println("---");
//        }


        System.out.println("DONE!");
    }
}
