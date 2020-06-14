package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import de.tu_berlin.imolcean.tdm.deployment.MigrationDeployer;
import de.tu_berlin.imolcean.tdm.imports.ExcelImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    @Qualifier("InternalDataSource")
    private SQLServerDataSource internalDs;

    @Autowired
    @Qualifier("ExternalDataSource")
    private SQLServerDataSource externalDs;

    @Autowired
    private SchemaExtractor schemaExtractor;

    @Autowired
    private ExcelImporter excelImporter;

    @Autowired
    private MigrationDeployer deployer;

    @Value("${app.data.excel.path}")
    private String excelDir;

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
//        Catalog internalDb = schemaExtractor.extractDboTables(internalDs);
//        Catalog externalDb = schemaExtractor.extractDboTables(externalDs);
//
//        final DiffNode diff = new SchemaDifferBuilder().build().compare(internalDb, externalDb);
//
//        SchemaDiffPrinter.print(diff);

        excelImporter.importDirectory(Paths.get(excelDir));

        Collections.sort((List<String>) excelImporter.filledTables);
        excelImporter.filledTables.forEach(System.out::println);
        System.out.println("Total: " + excelImporter.filledTables.size());

//        deployer.deploy();

        System.out.println("DONE!");
        System.exit(0);
    }
}
