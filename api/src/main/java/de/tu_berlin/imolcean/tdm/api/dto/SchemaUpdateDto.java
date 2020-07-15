package de.tu_berlin.imolcean.tdm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchemaUpdateDto
{
    List<String> addedTables;
    List<String> changedTables;
    List<String> deletedTables;
}
