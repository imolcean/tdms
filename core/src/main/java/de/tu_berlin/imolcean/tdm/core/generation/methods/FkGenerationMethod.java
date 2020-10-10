package de.tu_berlin.imolcean.tdm.core.generation.methods;

import lombok.extern.java.Log;
import schemacrawler.schema.Column;

@Log
public class FkGenerationMethod
{
    public Object pick(Column pkColumn, boolean postpone)
    {
        if(postpone)
        {
            // TODO Generate random value based on Column type
        }
        else
        {
            // TODO Get all values of pkColumn and pick one
        }

        return null;
    }

    public Object pick(Column pkColumn)
    {
        return pick(pkColumn, false);
    }
}
