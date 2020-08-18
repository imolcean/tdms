package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.DataSourceWrapper;

public class DataSourceMapper
{
    public static DataSourceDto toDto(DataSourceWrapper ds)
    {
        return new DataSourceDto(ds.getDriverClassName(), ds.getUrl(), ds.getDatabase(), ds.getUsername(), ds.getPassword());
    }
}
