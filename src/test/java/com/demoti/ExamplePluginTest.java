package com.demoti;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
    public static void main(String[] args) throws Exception
    {
        // KEY FIX: Make sure this says ActionCounterPlugin.class
        // If it says ExamplePlugin.class, your code will never load!
        ExternalPluginManager.loadBuiltin(ActionCounterPlugin.class);

        RuneLite.main(args);
    }
}