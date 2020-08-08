package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableContentDto
{
    private String tableName;
    private List<String> columnNames;
    private List<Object[]> data;
}
