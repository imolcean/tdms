package io.github.imolcean.tdms.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationMethodParamDescription
{
    private String name;
    private Class<?> type;
    private boolean required;
}
