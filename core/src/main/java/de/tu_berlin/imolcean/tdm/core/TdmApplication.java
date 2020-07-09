package de.tu_berlin.imolcean.tdm.core;

import de.danielbechler.diff.node.DiffNode;
import de.tu_berlin.imolcean.tdm.core.deployment.MigrationDeployer;
import de.tu_berlin.imolcean.tdm.core.utils.SchemaDiffPrinter;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import schemacrawler.schema.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    private DataSourceService dsService;

    @Autowired
    private SchemaService schemaService;

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


//        Catalog internalDb = schemaService.getSchema(dsService.getInternalDataSource());
//        Catalog importDb = schemaService.getSchema(dsService.getImportDataSource());
//
//        final DiffNode diff = new SchemaDifferBuilder().build().compare(importDb, internalDb);
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
