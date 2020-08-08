package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableContentDto
{
    private String tableName;
    private List<String> columnNames;
    private List<Object[]> data;
}
