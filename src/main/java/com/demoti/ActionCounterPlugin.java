package com.demoti;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "De-Motivator",
        description = "Tracks actions based on Chat Messages and XP",
        tags = {"counter", "skill"}
)
public class ActionCounterPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ActionCounterOverlay overlay;

    @Inject
    private ActionCounterConfig config;

    @Inject
    private Client client;

    // --- MEMORY ---
    private final Map<Skill, Integer> xpPerActionMap = new HashMap<>();
    private final Map<Skill, String> actionNameMap = new HashMap<>();
    private final Map<Skill, Integer> previousXpMap = new HashMap<>();

    // NEW: Track the single active skill
    private Skill currentSkill = null;

    // --- CHAT PATTERNS (Same as before) ---
    private static final Pattern WOODCUTTING_PATTERN = Pattern.compile("You get some (.*?)\\.");
    private static final Pattern FISHING_PATTERN = Pattern.compile("You catch (?:a|some) (.*?)\\.");
    private static final Pattern MINING_PATTERN = Pattern.compile("You manage to mine some (.*?)\\.");
    private static final Pattern PRAYER_PATTERN = Pattern.compile("You bury the (.*?)\\.");
    private static final Pattern COOKING_PATTERN = Pattern.compile("You successfully cook (?:a|some) (.*?)\\.");
    private static final Pattern FIREMAKING_PATTERN = Pattern.compile("You light the (.*?)\\.");
    private static final String FIREMAKING_NORMAL_LOGS = "The fire catches and the logs begin to burn.";
    private static final Pattern HERBLORE_CLEAN_PATTERN = Pattern.compile("You clean the (.*?)\\.");
    private static final Pattern CRAFTING_GEM_PATTERN = Pattern.compile("You cut the (.*?)\\.");
    private static final Pattern FLETCHING_PATTERN = Pattern.compile("You carefully cut the wood into (?:a|some) (.*?)\\.");
    private static final Pattern SMITHING_SMELT_PATTERN = Pattern.compile("You retrieve a bar of (.*?)\\.");
    private static final Pattern SMITHING_ANVIL_PATTERN = Pattern.compile("You hammer the .*? and make (?:a|an|some) (.*?)\\.");
    private static final Pattern RUNECRAFT_PATTERN = Pattern.compile("You bind the temple's power into (.*?)\\.");

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        xpPerActionMap.clear();
        actionNameMap.clear();
        previousXpMap.clear();
        currentSkill = null;
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) return;

        String message = event.getMessage();

        // Check patterns... if found, update currentSkill!
        if (checkPattern(message, WOODCUTTING_PATTERN, Skill.WOODCUTTING)) return;
        if (checkPattern(message, FISHING_PATTERN, Skill.FISHING)) return;
        if (checkPattern(message, MINING_PATTERN, Skill.MINING)) return;
        if (checkPattern(message, PRAYER_PATTERN, Skill.PRAYER)) return;
        if (checkPattern(message, COOKING_PATTERN, Skill.COOKING)) return;

        if (message.equals(FIREMAKING_NORMAL_LOGS)) {
            actionNameMap.put(Skill.FIREMAKING, "logs");
            currentSkill = Skill.FIREMAKING; // <--- Switch active skill
            return;
        }
        if (checkPattern(message, FIREMAKING_PATTERN, Skill.FIREMAKING)) return;

        if (checkPattern(message, HERBLORE_CLEAN_PATTERN, Skill.HERBLORE)) return;
        if (checkPattern(message, CRAFTING_GEM_PATTERN, Skill.CRAFTING)) return;
        if (checkPattern(message, FLETCHING_PATTERN, Skill.FLETCHING)) return;

        if (checkPattern(message, SMITHING_SMELT_PATTERN, Skill.SMITHING)) return;
        if (checkPattern(message, SMITHING_ANVIL_PATTERN, Skill.SMITHING)) return;
        if (checkPattern(message, RUNECRAFT_PATTERN, Skill.RUNECRAFT)) return;
    }

    private boolean checkPattern(String message, Pattern pattern, Skill skill)
    {
        Matcher matcher = pattern.matcher(message);
        if (matcher.find())
        {
            actionNameMap.put(skill, matcher.group(1));
            currentSkill = skill; // <--- Switch active skill whenever we detect an action
            return true;
        }
        return false;
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        Skill skill = event.getSkill();
        int currentXp = event.getXp();
        int previousXp = previousXpMap.getOrDefault(skill, -1);

        if (previousXp == -1)
        {
            previousXpMap.put(skill, currentXp);
            return;
        }

        int xpDrop = currentXp - previousXp;
        if (xpDrop > 0)
        {
            xpPerActionMap.put(skill, xpDrop);
            previousXpMap.put(skill, currentXp);
            currentSkill = skill; // <--- Switch active skill on XP drop too
        }
    }

    public int getXpPerAction(Skill skill)
    {
        return xpPerActionMap.getOrDefault(skill, 0);
    }

    public String getCurrentActionName(Skill skill)
    {
        return actionNameMap.getOrDefault(skill, "Actions");
    }

    // NEW: Allow overlay to ask what the active skill is
    public Skill getCurrentSkill()
    {
        return currentSkill;
    }

    @Provides
    ActionCounterConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ActionCounterConfig.class);
    }
}