package io.github.imolcean.tdms.core.controllers.mappers;

import io.github.imolcean.tdms.api.dto.DataSourceDto;
import io.github.imolcean.tdms.api.dto.StageDto;

public class StageMapper
{
    public static StageDto toDto(String name, DataSourceDto datasource)
    {
        return new StageDto(name, datasource);
    }
}
