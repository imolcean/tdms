package io.github.imolcean.tdms.core;

import io.github.imolcean.tdms.api.exceptions.InvalidStageNameException;

public class StageSelectionContextHolder
{
    private static String stageName = null;

    public static void setStageName(String name)
    {
        if(name.equalsIgnoreCase("internal")
                || name.equalsIgnoreCase("tmp")
                || name.equalsIgnoreCase("current"))
        {
            throw new InvalidStageNameException(name);
        }

        stageName = name;
    }

    public static String getStageName()
    {
        return stageName;
    }

    public static void clearStageName()
    {
        stageName = null;
    }
}
