package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import de.tu_berlin.imolcean.tdm.utils.SchemaPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import schemacrawler.schema.*;

import java.util.Collection;

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

    public static void main(String[] args)
    {
        SpringApplication.run(TdmApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
//        Collection<Table> internalDbTables = schemaExtractor.extractDboTables(internalDs, internalDs.getDatabaseName());
//        SchemaPrinter.print(internalDbTables);

        Collection<Table> externalRu2DbTables = schemaExtractor.extractDboTables(externalRu2Ds, externalRu2Ds.getDatabaseName());
        SchemaPrinter.print(externalRu2DbTables);
    }
}
