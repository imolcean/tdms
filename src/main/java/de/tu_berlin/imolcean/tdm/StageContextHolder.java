package de.tu_berlin.imolcean.tdm;

public class StageContextHolder
{
    private static ThreadLocal<String> stageName;

    public static void setStageName(String name)
    {
        stageName.set(name);
    }

    public static String getStageName()
    {
        return stageName == null ? null : stageName.get();
    }

    public static void clearStageName()
    {
        stageName.remove();
    }
}
