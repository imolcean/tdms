package io.github.imolcean.tdms.core.controllers.mappers;

import io.github.imolcean.tdms.api.dto.DataSourceDto;
import io.github.imolcean.tdms.api.DataSourceWrapper;

public class DataSourceMapper
{
    public static DataSourceDto toDto(DataSourceWrapper ds)
    {
        return new DataSourceDto(ds.getDriverClassName(), ds.getUrl(), ds.getDatabase(), ds.getUsername(), ds.getPassword());
    }
}
