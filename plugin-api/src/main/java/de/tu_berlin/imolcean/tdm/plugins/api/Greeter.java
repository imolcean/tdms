package de.tu_berlin.imolcean.tdm.plugins.api;

import org.pf4j.ExtensionPoint;

public interface Greeter extends ExtensionPoint
{
    void greet();
}
