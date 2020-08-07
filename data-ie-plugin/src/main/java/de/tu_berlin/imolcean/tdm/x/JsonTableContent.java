package de.tu_berlin.imolcean.tdm.x;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// TODO Reuse DTO classes?

@Data
@AllArgsConstructor
public class JsonTableContent
{
    private String tableName;
    private List<String> columnNames;
    private List<Object[]> rows;
}
