package io.github.imolcean.tdms.core.controllers.mappers;

import io.github.imolcean.tdms.api.ValueLibrary;
import io.github.imolcean.tdms.api.dto.ValueListDto;

public class ValueListMapper
{
    public static ValueListDto toDto(ValueLibrary library)
    {
        return new ValueListDto(library.getId(), library.isList(), library.getList());
    }
}
