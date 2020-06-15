package de.tu_berlin.imolcean.tdm;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class StageDataSourceRouter extends AbstractRoutingDataSource
{
    @Override
    protected Object determineCurrentLookupKey()
    {
        return StageContextHolder.getStageName();
    }

    // TODO ?
    // TODO Load all ds configs at startup
    // TODO When asked for an absent ds, try to load it
}
