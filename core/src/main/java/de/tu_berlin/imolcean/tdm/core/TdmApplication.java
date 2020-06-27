package de.tu_berlin.imolcean.tdm.core;

import de.danielbechler.diff.node.DiffNode;
import de.tu_berlin.imolcean.tdm.core.deployment.MigrationDeployer;
import de.tu_berlin.imolcean.tdm.api.plugins.SchemaAwareImporter;
import de.tu_berlin.imolcean.tdm.core.utils.SchemaDiffPrinter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import schemacrawler.schema.Catalog;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    @Qualifier("InternalDataSource")
    private DataSourceProxy internalDs;

    @Autowired
    private StageDataSourceManager stageDsManager;

    @Autowired
    private SchemaExtractor schemaExtractor;

    @Autowired
    private MigrationDeployer deployer;

    @Autowired
    private SpringPluginManager plugins;

    @Value("${app.data.excel.path}")
    private String excelImportDir;

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


//        SchemaAwareImporter excelImporter = plugins.getExtensions(SchemaAwareImporter.class).stream()
//                .findFirst()
//                .orElseThrow();
//
//        try(Connection connection = internalDs.getConnection())
//        {
//            excelImporter.importPath(Path.of(excelImportDir), connection, schemaExtractor.extractDboTables(internalDs).getTables());
//        }


//        deployer.deploy();


        System.out.println("DONE!");
    }
}
