package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnDto
{
    private String name;

    private String type;

    private boolean primaryKey;

    // TODO FK
}
