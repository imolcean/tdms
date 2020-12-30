package io.github.imolcean.tdms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueListDto
{
    private String name;
    private Boolean isList;
    private List<?> options;
}
