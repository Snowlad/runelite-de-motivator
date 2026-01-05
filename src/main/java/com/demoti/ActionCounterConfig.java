package com.demoti;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("demotivator")
public interface ActionCounterConfig extends Config
{
    @ConfigItem(
            keyName = "targetLevel",
            name = "Target Level",
            description = "The level you are aiming for"
    )
    default int targetLevel()
    {
        return 99;
    }
}