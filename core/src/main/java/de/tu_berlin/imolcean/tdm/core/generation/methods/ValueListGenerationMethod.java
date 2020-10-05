package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;

import java.util.List;

@Log
public class ValueListGenerationMethod
{
    public Object pick(List<Object> list)
    {
        log.fine(String.format("Picking from a list of %s elements", list == null ? null : list.size()));

        if(list == null || list.size() == 0)
        {
            return null;
        }

        int randomIndex = new RandIntegerGenerationMethod().generate(0, list.size());

        return list.get(randomIndex);
    }
}
