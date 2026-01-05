package com.demoti;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class ActionCounterOverlay extends Overlay
{
    private final Client client;
    private final ActionCounterPlugin plugin;
    private final ActionCounterConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public ActionCounterOverlay(Client client, ActionCounterPlugin plugin, ActionCounterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGHEST);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(LineComponent.builder()
                .left("De-Motivator")
                .leftColor(Color.ORANGE)
                .build());

        // 1. Get ONLY the currently active skill
        Skill skill = plugin.getCurrentSkill();

        if (skill != null)
        {
            int xpPerAction = plugin.getXpPerAction(skill);

            // Only draw if we have valid data for this active skill
            if (xpPerAction > 0)
            {
                int currentXp = client.getSkillExperience(skill);
                int targetLevel = config.targetLevel();
                int currentLevel = Experience.getLevelForXp(currentXp);

                // Cap target level logic
                if (currentLevel >= targetLevel) targetLevel = currentLevel + 1;
                if (targetLevel > 99) targetLevel = 99;

                int targetXp = Experience.getXpForLevel(targetLevel);
                int xpToTarget = targetXp - currentXp;
                int xpTo99 = Experience.getXpForLevel(99) - currentXp;

                if (xpTo99 > 0)
                {
                    int actionsToTarget = (int) Math.ceil((double) xpToTarget / xpPerAction);
                    int actionsTo99 = (int) Math.ceil((double) xpTo99 / xpPerAction);
                    String name = plugin.getCurrentActionName(skill);

                    // Line 1: Target
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Target (" + targetLevel + "):")
                            .right(actionsToTarget + " " + name)
                            .rightColor(Color.GREEN)
                            .build());

                    // Line 2: Level 99
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Level 99:")
                            .right(String.valueOf(actionsTo99))
                            .rightColor(Color.CYAN)
                            .build());

                    return panelComponent.render(graphics);
                }
            }
        }

        // 2. If no active skill, show hint
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:")
                .right("Do an action!")
                .rightColor(Color.YELLOW)
                .build());

        return panelComponent.render(graphics);
    }
}