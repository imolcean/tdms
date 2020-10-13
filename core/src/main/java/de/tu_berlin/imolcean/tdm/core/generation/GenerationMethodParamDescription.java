package de.tu_berlin.imolcean.tdm.core.generation;

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
