package io.github.imolcean.tdms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StageDto
{
    private String name;
    private DataSourceDto datasource;
}
