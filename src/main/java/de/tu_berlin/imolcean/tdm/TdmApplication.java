package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TdmApplication implements CommandLineRunner
{
    @Autowired
    @Qualifier("InternalDataSource")
    private SQLServerDataSource internalDs;

    @Autowired
    @Qualifier("ExternalRu2DataSource")
    private SQLServerDataSource externalRu2Ds;

    @Autowired
    SchemaExtractor schemaExtractor;

    @Autowired
    private MigrationDeployer deployer;

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
//        Catalog internalDb = schemaExtractor.extractDboTables(internalDs);
//        Catalog externalRu2Db = schemaExtractor.extractDboTables(externalRu2Ds);
//
//        final DiffNode diff = new SchemaDifferBuilder().build().compare(internalDb, externalRu2Db);
//
//        SchemaDiffPrinter.print(diff);

        deployer.deploy();

        System.out.println("DONE!");
    }
}
