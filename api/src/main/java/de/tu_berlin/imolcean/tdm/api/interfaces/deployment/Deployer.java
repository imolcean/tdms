package de.tu_berlin.imolcean.tdm.api.interfaces.deployment;

import de.tu_berlin.imolcean.tdm.api.interfaces.PublicInterface;
import org.pf4j.ExtensionPoint;

import javax.sql.DataSource;

public interface Deployer extends PublicInterface, ExtensionPoint
{
    /**
     * Performs deployment of the data stored in the source database into the target database.
     *
     * The content of all non-empty tables is copied.
     * Implementations of this method should guarantee that the data is either copied
     * completely or not at all, in case an error occurs.
     *
     * @param src source database, usually this will be the internal DB
     * @param target target database, usually this will be the DB of the current stage
     */
    void deploy(DataSource src, DataSource target) throws Exception;
}
