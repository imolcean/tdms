package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.plugins.api.Greeter;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class WelcomePlugin extends Plugin
{
    public WelcomePlugin(PluginWrapper wrapper)
    {
        super(wrapper);
    }

    @Extension
    public static class WelcomeGreeter implements Greeter
    {
        @Override
        public void greet()
        {
            System.out.println("Welcome!");
        }
    }

    @Extension
    public static class WazzupGreeter implements Greeter
    {
        @Override
        public void greet()
        {
            System.out.println("Wazzup!");
        }
    }
}
