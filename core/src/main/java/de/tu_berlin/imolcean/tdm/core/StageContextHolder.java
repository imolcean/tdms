package de.tu_berlin.imolcean.tdm.core;

public class StageContextHolder
{
    private static String stageName = null;

    public static void setStageName(String name)
    {
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
