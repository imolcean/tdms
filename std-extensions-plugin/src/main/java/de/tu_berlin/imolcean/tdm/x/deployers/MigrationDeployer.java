package de.tu_berlin.imolcean.tdm.x.deployers;

import de.tu_berlin.imolcean.tdm.api.interfaces.deployment.Deployer;
import de.tu_berlin.imolcean.tdm.api.services.SchemaService;
import de.tu_berlin.imolcean.tdm.api.services.TableContentService;
import lombok.extern.java.Log;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import schemacrawler.schema.Table;

import javax.sql.DataSource;
import java.util.List;

/**
 * Performs migration-based deployment of the test data into a target database.
 *
 * Migration is the default deployment method for test data. It means that content of
 * every non-empty table in the source database will be copied into the target database.
 *
 * Note that the target database does not necessarily have to be empty because it will be cleared
 * before deployment in order to guarantee that no primary-key or other constraints are
 * violated during the data is copied. This {@link Deployer} also does not perform any checks of schema
 * similarity between the source and target databases. The two, however, need to have compliant schemas
 * not to cause SQL errors during the data is copied.
 */
@Component
@Extension
@Log
public class MigrationDeployer implements Deployer
{
    @Autowired
    private SchemaService schemaService;

    @Autowired
    private TableContentService tableContentService;

    @Override
    public void deploy(DataSource src, DataSource target) throws Exception
    {
        log.info("Starting deployment");
        log.fine("Looking for non-empty tables in the source DB");

        List<Table> tablesToCopy = schemaService.getTables(src, schemaService.getOccupiedTableNames(src));
        if(tablesToCopy.isEmpty())
        {
            throw new IllegalStateException("There is no data to deploy");
        }

        log.fine("Clearing the target DB");

        List<Table> tablesToClear = schemaService.getTables(target, schemaService.getOccupiedTableNames(target));
        tableContentService.clearTables(target, tablesToClear);

        log.fine("Copying data from the source DB into the target DB");

        tableContentService.copyData(src, target, schemaService.getTables(src, schemaService.getOccupiedTableNames(src)));

        log.info("Deployment finished");
    }
}
