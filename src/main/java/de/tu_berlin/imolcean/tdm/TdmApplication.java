package de.tu_berlin.imolcean.tdm;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import schemacrawler.schema.*;

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
        Catalog internalDb = schemaExtractor.extractDboTables(internalDs);
        Catalog externalRu2Db = schemaExtractor.extractDboTables(externalRu2Ds);

        final DiffNode diff = new SchemaDifferBuilder().build().compare(internalDb, externalRu2Db);

        diff.visit((node, visit) -> {
            final DiffNode.State nodeState = node.getState();
            final boolean print = DatabaseObject.class.isAssignableFrom(node.getValueType());

            if (print)
            {
                System.out.println(node.getPath() + " (" + nodeState + ")");
            }

            if (Table.class.isAssignableFrom(node.getValueType()) && nodeState != DiffNode.State.CHANGED)
            {
                visit.dontGoDeeper();
            }

            if (Column.class.isAssignableFrom(node.getValueType()))
            {
                visit.dontGoDeeper();
            }
        });

        System.out.println(diff.hasChanges());
        System.out.println(diff.hasChildren());

        System.out.println("=====================================================");

        diff.visit((node, visit) -> System.out.println(node.getPath() + " => " + node.getState()));

        System.out.println("DONE!");
    }
}
