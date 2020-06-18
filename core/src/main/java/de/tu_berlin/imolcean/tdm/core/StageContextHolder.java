package de.tu_berlin.imolcean.tdm.core;

public class StageContextHolder
{
    private static final ThreadLocal<String> stageName = new ThreadLocal<>();

    public static void setStageName(String name)
    {
        stageName.set(name);
    }

    public static String getStageName()
    {
        return stageName.get();
    }

    public static void clearStageName()
    {
        stageName.remove();
    }
}
