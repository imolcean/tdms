package de.tu_berlin.imolcean.tdm.api.plugins;

import javax.sql.DataSource;

// TODO JavaDoc
public interface SchemaUpdater
{
    void updateSchema(DataSource ds);
}
