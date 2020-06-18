package it.berlusconi.solvio.tdm;

import de.tu_berlin.imolcean.tdm.plugins.api.Greeter;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class BerlusconiPlugin extends Plugin
{
    public BerlusconiPlugin(PluginWrapper wrapper)
    {
        super(wrapper);
    }

    @Override
    public void start()
    {
        System.out.println("Berlusconi staerted");
    }

    @Override
    public void stop()
    {
        System.out.println("Berlusconi stopped");
    }

    @Override
    public void delete()
    {
        System.out.println("Berlusconi deleted");
    }

    @Extension
    public static class CiaoGreeter implements Greeter
    {
        @Override
        public void greet()
        {
            System.out.println("Ciao!");
        }
    }
}
