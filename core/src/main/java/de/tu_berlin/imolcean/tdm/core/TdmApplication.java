package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.core.deployment.MigrationDeployer;
import de.tu_berlin.imolcean.tdm.core.imports.ExcelImporter;
import de.tu_berlin.imolcean.tdm.plugins.api.Greeter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    @Qualifier("InternalDataSource")
    private DataSource internalDs;

    @Autowired
    private StageDataSourceManager stageDsManager;

    @Autowired
    private SchemaExtractor schemaExtractor;

    @Autowired
    private ExcelImporter excelImporter;

    @Autowired
    private MigrationDeployer deployer;

    // TODO Pack into ExcelImporter
    @Value("${app.data.excel.path}")
    private String excelDir;

    @Autowired
    private SpringPluginManager plugins;

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        StageContextHolder.setStageName("dev0");


//        Catalog internalDb = schemaExtractor.extractDboTables(internalDs);
//        Catalog externalDb = schemaExtractor.extractDboTables(stageDsManager.getCurrentStageDataSource());
//
//        final DiffNode diff = new SchemaDifferBuilder().build().compare(internalDb, externalDb);
//        SchemaDiffPrinter.print(diff);


//        excelImporter.importDirectory(Paths.get(excelDir));
//
//        Collections.sort((List<String>) excelImporter.filledTables);
//        excelImporter.filledTables.forEach(System.out::println);
//        System.out.println(String.format("Imported %d tables ", excelImporter.filledTables.size()));


//        deployer.deploy();

        List<Greeter> greeters = plugins.getExtensions(Greeter.class);
        for(Greeter g : greeters)
        {
            System.out.print(g.getClass() + ": ");
            g.greet();
        }


        System.out.println("DONE!");
    }
}
