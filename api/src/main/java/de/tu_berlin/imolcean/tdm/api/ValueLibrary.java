package de.tu_berlin.imolcean.tdm.api;

import java.util.HashMap;

public class ValueLibrary extends HashMap<String, Object>
{
    public String getId()
    {
        return (String) this.get("_id");
    }

    public boolean isList()
    {
        return this.get("_list") != null;
    }

    public Object[] getList()
    {
        return (Object[]) this.get("_list");
    }
}
