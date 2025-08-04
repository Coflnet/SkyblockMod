package de.torui.coflsky.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import de.torui.coflsky.CoflSky;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Comprehensive OneConfig configuration for CoflSky.
 * <p>
 * This mirrors the settings structure maintained in the separate
 * <code>Coding/Windsurf/Cofl-Oneconfig</code> project. Only the first half
 * (General + Mod categories) is ported in this commit – remaining
 * categories will be added subsequently to keep the diff readable.
 */
public class CoflConfig extends Config {

    /*  ───────────────────────────── GENERAL ───────────────────────────── */

    private transient static final String GENERAL = "General";
    private transient static final String MOD = "Mod";
    private transient static final String VISIBILITY = "Visibility";
    private transient static final String PRIVACY = "Privacy";

    @Header(text = "Flipping Logic", size = OptionSize.DUAL)
    public static boolean _hFlippingLogic = false;

    @Info(text = "General flipping-related parameters", size = OptionSize.DUAL, type = InfoType.INFO)
    public static boolean _infoGen = false;

    // ─── Flipping logic ──────────────────────────────────────────────────
    @Switch(name = "Based On Lowest BIN", description = "Calculate profit using current lowest BIN price", category = GENERAL)
    public static boolean lbin = false;

    @Switch(name = "Only BIN Auctions", description = "Ignore auctions without BIN; snipe BINs only", category = GENERAL)
    public static boolean onlyBin = true;

    @Switch(name = "Whitelist After Main Filters", description = "Apply whitelist filters after standard ones", category = GENERAL)
    public static boolean whitelistAftermain = false;

    @Switch(name = "Disable Flips", description = "Completely disable flip suggestions", category = GENERAL)
    public static boolean DisableFlips = false;

    @Switch(name = "Debug Mode", description = "Enable verbose logging for troubleshooting", category = GENERAL)
    public static boolean DebugMode = false;

    @Switch(name = "Block High-Competition Flips", description = "Hide flips with too many competing listings", category = GENERAL)
    public static boolean blockHighCompetition = false;

    @Slider(name = "Minimum Profit (Millions)", description = "Minimum raw coin profit required (in millions of coins)", min = 0f, max = 50f, step = 1, category = GENERAL)
    public static int minProfit = 3;

    @Slider(name = "Minimum Profit %", description = "Minimum percentage profit required", min = 0f, max = 100f, step = 1, category = GENERAL)
    public static int minProfitPercent = 10;

    @Slider(name = "Minimum Volume / 24h", description = "Minimum sales per day to qualify", min = 0f, max = 30f, step = 1, category = GENERAL)
    public static int minVolume = 0;

    @Slider(name = "Maximum Cost (Millions)", description = "Upper price limit (in millions of coins)", min = 0f, max = 10_000f, step = 1, category = GENERAL)
    public static int maxCost = 10_000; // 10,000 M = 10B

    /* Helper getters */
    public static long getMinProfitCoins() {
        return minProfit * 1_000_000L;
    }

    public static long getMaxCostCoins() {
        return maxCost * 1_000_000L;
    }

    // ─── Highlighting ────────────────────────────────────────────────────
    @Switch(name = "Highlight Filter Matches (AH/Trades)")
    public static boolean loreHighlightFilterMatch = false;

    @Slider(name = "Min Profit for Highlight", min = 0f, max = 10_000_000f, step = 100_000)
    public static float loreMinProfitForHighlight = 0f;

    @Switch(name = "Disable Highlighting")
    public static boolean loreDisableHighlighting = false;

    // ─── Price Finder selection ─────────────────────────────────────────
    @Header(text = "Allowed Price Finder Algorithms", size = OptionSize.DUAL)
    public static boolean _hFinders = false;

    @Switch(name = "Flipper")
    public static boolean finders = true;

    @Switch(name = "Sniper")
    public static boolean finderSniper = true;

    @Switch(name = "Sniper (Median)")
    public static boolean finderSniperMedian = true;

    @Switch(name = "User (Whitelist)")
    public static boolean finderUserWhitelist = true;

    @Switch(name = "TFM")
    public static boolean finderTFM = true;

    @Switch(name = "Stonks")
    public static boolean finderStonks = true;

    @Switch(name = "Craftcost")
    public static boolean finderCraftcost = true;


    /*  ───────────────────────────── MOD ───────────────────────────────── */

    @Header(text = "", size = OptionSize.DUAL)
    public static boolean _spModChatTop = false;

    @Header(text = "Chat & Sounds", size = OptionSize.DUAL)
    public static boolean _hModChat = false;

    @Info(text = "Mod chat output & sound alerts", size = OptionSize.DUAL, type = InfoType.INFO)
    public static boolean _infoModChat = false;

    // ─── Chat & Sound ───────────────────────────────────────────────────
    @Switch(name = "Enable Chat")
    public static boolean modchat = false;

    @Switch(name = "Display Just Profit")
    public static boolean modjustProfit = true;

    @Switch(name = "Play Sound On Flip")
    public static boolean modsoundOnFlip = true;

    @Switch(name = "Short Numbers (M/k)")
    public static boolean modshortNumbers = true;

    @Switch(name = "Short Item Names")
    public static boolean modshortNames = false;

    @Switch(name = "Block Ten-Second Msg")
    public static boolean modblockTenSecMsg = false;

    @Text(name = "Custom Format", placeholder = "%item% -> %profit% (e.g. Drill -> 3.2m)", secure = false)
    public static String modformat = "";

    @Text(name = "Custom Blocked Format", placeholder = "Blocked %item% (e.g. Blocked Drill)", secure = false)
    public static String modblockedFormat = "";

    /* Timer Widget */
    @Header(text = "Timer Widget", size = OptionSize.DUAL)
    public static boolean _hModTimer = false;

    @Info(text = "HUD timer display options", size = OptionSize.DUAL, type = InfoType.INFO)
    public static boolean _infoTimer = false;

    @Switch(name = "Display Timer")
    public static boolean modcountdown = true;

    @Slider(name = "Timer X Position (%)", min = 0f, max = 100f, step = 1)
    public static float modtimerX = 0f;

    @Slider(name = "Timer Y Position (%)", min = 0f, max = 100f, step = 1)
    public static float modtimerY = 0f;

    @Slider(name = "Timer Show Seconds Before Update", min = 0f, max = 60f, step = 1)
    public static float modtimerSeconds = 0f;

    @Slider(name = "Timer Scale", min = 0f, max = 4f, step = 1)
    public static int modtimerScale = 1;

    @Slider(name = "Timer Precision (digits)", min = 0f, max = 5f, step = 1)
    public static float modtimerPrecision = 0f;

    @Text(name = "Timer Prefix", placeholder = "Next flip in ", secure = false)
    public static String modtimerPrefix = "";

    /* Behaviour toggles */
    @Header(text = "Behaviour Toggles", size = OptionSize.DUAL)
    public static boolean _hModBehaviour = false;

    @Info(text = "Advanced behaviour tweaks", size = OptionSize.DUAL, type = InfoType.INFO)
    public static boolean _infoBeh = false;

    @Slider(name = "Minutes Between Blocked Msg", min = 0f, max = 127f, step = 1)
    public static float modblockedMsg = 0f;

    @Slider(name = "Max % Of Purse Per Flip", min = 0f, max = 100f, step = 1)
    public static float modmaxPercentOfPurse = 0f;

    @Slider(name = "AH List Time Target (h)", min = 0f, max = 24f, step = 1)
    public static float modahListHours = 0f;

    @Slider(name = "Max Flip Items in Inventory", min = 0f, max = 54f, step = 1)
    public static float modmaxItemsInInventory = 0f;

    @Switch(name = "No Bed Delay")
    public static boolean modnoBedDelay = false;

    @Switch(name = "Streamer Mode")
    public static boolean modstreamerMode = false;

    @Switch(name = "Auto-Start Flipper")
    public static boolean modautoStartFlipper = false;

    @Switch(name = "Normal Sold Flips")
    public static boolean modnormalSoldFlips = false;

    @Switch(name = "Temp Blacklist Spam")
    public static boolean modtempBlacklistSpam = false;

    @Switch(name = "AH-Data-Only Mode")
    public static boolean moddataOnlyMode = false;

    @Switch(name = "Quick Sell")
    public static boolean modquickSell = false;

    @Switch(name = "Disable Spam Protection (⚠)")
    public static boolean moddisableSpamProtection = false;

    /*  ───────────────────────── VISIBILITY ───────────────────────────── */

    @Header(text = "", size = OptionSize.DUAL)
    public static boolean _spVisTop = false;

    @Header(text = "Visibility Options", size = OptionSize.DUAL)
    public static boolean _hVisibility = false;

    @Info(text = "Item info display options", size = OptionSize.DUAL, type = InfoType.INFO)
    public static boolean _infoVis = false;

    @Switch(name = "Show Cost")
    public static boolean showcost = true;

    @Switch(name = "Show Estimated Profit")
    public static boolean showestProfit = true;

    @Switch(name = "Show Lowest BIN")
    public static boolean showlbin = false;

    @Switch(name = "Show 2nd Lowest BIN")
    public static boolean showslbin = false;

    @Switch(name = "Show Median Price")
    public static boolean showmedPrice = false;

    @Switch(name = "Show Seller Name")
    public static boolean showseller = false;

    @Switch(name = "Show Volume")
    public static boolean showvolume = true;

    @Switch(name = "Show Profit %")
    public static boolean showprofitPercent = true;

    @Switch(name = "Show Profit (abs)")
    public static boolean showprofit = false;

    @Switch(name = "Show Seller Open Button")
    public static boolean showsellerOpenBtn = true;

    @Switch(name = "Show Item Lore")
    public static boolean showlore = true;

    @Switch(name = "Hide Sold Auctions")
    public static boolean showhideSold = false;

    @Switch(name = "Hide Manipulated Items")
    public static boolean showhideManipulated = false;

    @Slider(name = "Extra Info Fields", min = 0f, max = 5f, step = 1)
    public static float showextraFields = 0f;

    /*  ────────────────────────── PRIVACY ─────────────────────────────── */

    @Header(text = "", size = OptionSize.DUAL)
    public static boolean _spPrivTop = false;

    @Header(text = "Privacy Settings", size = OptionSize.DUAL)
    public static boolean _hPrivacy = false;

    @Info(text = "Telemetry & data collection", size = OptionSize.DUAL, type = InfoType.WARNING)
    public static boolean _infoPriv = false;

    @Switch(name = "Collect Chat")
    public static boolean privacyCollectChat = true;

    @Switch(name = "Collect Inventory")
    public static boolean privacyCollectInventory = true;

    @Switch(name = "Disable Trade Storing")
    public static boolean privacyDisableTradeStoring = false;

    @Switch(name = "Disable Kuudra Tracking")
    public static boolean privacyDisableKuudraTracking = false;

    @Switch(name = "Collect Tab")
    public static boolean privacyCollectTab = false;

    @Switch(name = "Collect Scoreboard")
    public static boolean privacyCollectScoreboard = true;

    @Switch(name = "Collect Inv Click")
    public static boolean privacyCollectInvClick = false;

    @Switch(name = "Collect Chat Clicks")
    public static boolean privacyCollectChatClicks = true;

    @Switch(name = "Collect Lobby Changes")
    public static boolean privacyCollectLobbyChanges = false;

    @Switch(name = "Collect Entities")
    public static boolean privacyCollectEntities = false;

    @Switch(name = "Collect Location")
    public static boolean privacyCollectLocation = false;

    @Switch(name = "Extend Descriptions")
    public static boolean privacyExtendDescriptions = true;

    @Switch(name = "Auto Start")
    public static boolean privacyAutoStart = true;

    /*  ──────────────────────────── Cache Helpers ─────────────────────────── */

    /**
     * Retrieve a boolean config value by its server key, falling back to the static default
     * value defined in this class. This avoids direct static-field references and ensures
     * live values from {@link SettingsCache} are respected.
     */
    public static boolean bool(String key, boolean fallback) {
        return SettingsCache.getBool(key, fallback);
    }

    public static int integer(String key, int fallback) {
        return SettingsCache.getInt(key, fallback);
    }

    public static long lng(String key, long fallback) {
        return SettingsCache.getLong(key, fallback);
    }

    public static double dbl(String key, double fallback) {
        return SettingsCache.getDouble(key, fallback);
    }

    public static String str(String key, String fallback) {
        return SettingsCache.getString(key, fallback);
    }

    /*  ──────────────────────────── 1-1 Name Aliases ───────────────────────── */

    // General
    public static boolean DisableFlips() { return bool("DisableFlips", DisableFlips); }
    public static boolean DebugMode()     { return bool("DebugMode", DebugMode); }
    public static boolean lbin()          { return bool("lbin", lbin); }
    public static boolean onlyBin()       { return bool("onlyBin", onlyBin); }
    public static boolean whitelistAftermain() { return bool("whitelistAftermain", whitelistAftermain); }
    public static boolean blockHighCompetition() { return bool("BlockHighCompetitionFlips", blockHighCompetition); }

    // Visibility
    public static boolean showcost()      { return bool("showcost", showcost); }
    public static boolean showprofitPercent() { return bool("showprofitPercent", showprofitPercent); }

    // Mod toggles (sample subset; add more as needed)
    public static boolean modjustProfit() { return bool("modjustProfit", modjustProfit); }
    public static boolean modchat()       { return bool("modchat", modchat); }
    public static boolean moddataOnlyMode() { return bool("moddataOnlyMode", moddataOnlyMode); }
    public static boolean finders() { return bool("finders", finders); }

    // Helper to expose cache snapshot for debugging
    public static java.util.Map<String,Object> dumpCache() { return SettingsCache.snapshot(); }

    /*  ───────────────────────── Constructor ──────────────────────────── */
    /* Singleton instance to avoid duplicate OneConfig menu registrations */
    public static final CoflConfig INSTANCE = new CoflConfig(true);

    /**
     * Opens the singleton GUI.
     */
    public static void openGuiStatic() {
        INSTANCE.openGui();
    }

    // private ctor flag to differentiate singleton creation
    private CoflConfig(boolean dummy) {
        super(new Mod(CoflSky.MODID, ModType.UTIL_QOL), CoflSky.MODID.toLowerCase() + "_config.json");
        initialize();
    }

    // legacy public constructor for code paths that still call new; delegates to singleton
    public CoflConfig() { this(true); }

    /*  ─────────── Integer alias sample ─────────── */
    public static int MinProfitPercent() { return integer("minProfitPercent", minProfitPercent); }

    @Override
    public void save() {
        super.save();
        pushUpdatesToServer();
    }

    /**
     * Compare current static field values to {@link SettingsCache} and send any updates
     * to the website via websocket using a Command of type "set".
     */
    private void pushUpdatesToServer() {
        try {
            java.util.Map<String, Object> before = SettingsCache.snapshot();
            java.util.Map<String, String> updateMap = new java.util.LinkedHashMap<>();
            for (java.lang.reflect.Field f : CoflConfig.class.getDeclaredFields()) {
                if ((f.getModifiers() & java.lang.reflect.Modifier.STATIC) == 0) continue;
                Class<?> type = f.getType();
                if (!(type.isPrimitive() || type == String.class)) continue;
                f.setAccessible(true);
                Object value = f.get(null);
                Object old = before.getOrDefault(f.getName(), null);
                String valStr;
                // convert millions to coins for maxCost
                if ("maxCost".equals(f.getName()) && value instanceof Number) {
                    valStr = String.valueOf(((Number) value).longValue() * 1_000_000L);
                } else {
                    valStr = String.valueOf(value);
                }
                String oldStr = old == null ? "" : String.valueOf(old);
                if (!valStr.equals(oldStr)) {
                    SettingsCache.put(f.getName(), value);
                    updateMap.put(f.getName(), valStr);
                }
            }
            if (!updateMap.isEmpty() && de.torui.coflsky.CoflSky.Wrapper != null) {
                de.torui.coflsky.commands.Command<java.util.Map<String,String>> cmd =
                        new de.torui.coflsky.commands.Command<>(de.torui.coflsky.commands.CommandType.uploadSettings, updateMap);
                de.torui.coflsky.CoflSky.Wrapper.SendMessage(cmd);
            }
        } catch (Exception ex) {
            System.err.println("Failed to push settings to server: " + ex.getMessage());
        }
    }
}
