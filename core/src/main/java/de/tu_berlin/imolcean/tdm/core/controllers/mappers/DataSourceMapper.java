package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.core.DataSourceProxy;

public class DataSourceMapper
{
    public static DataSourceDto toDto(DataSourceProxy ds)
    {
        return new DataSourceDto(ds.getDriverClassName(), ds.getUrl(), ds.getUsername(), ds.getPassword());
    }
}
