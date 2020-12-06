package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.DataSourceDto;
import de.tu_berlin.imolcean.tdm.api.dto.StageDto;

public class StageMapper
{
    public static StageDto toDto(String name, DataSourceDto datasource)
    {
        return new StageDto(name, datasource);
    }
}
