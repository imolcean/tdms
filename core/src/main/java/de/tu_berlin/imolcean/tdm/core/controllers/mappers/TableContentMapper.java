package de.tu_berlin.imolcean.tdm.core.controllers.mappers;

import de.tu_berlin.imolcean.tdm.api.dto.TableContentDto;

import java.util.List;

public class TableContentMapper
{
    public static TableContentDto toDto(List<Object[]> rows)
    {
        return new TableContentDto(rows);
    }
}
