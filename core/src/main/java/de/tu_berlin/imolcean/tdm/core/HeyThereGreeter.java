package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.plugins.api.Greeter;
import org.pf4j.Extension;

@Extension
public class HeyThereGreeter implements Greeter
{
    @Override
    public void greet()
    {
        System.out.println("Hey there!");
    }
}
