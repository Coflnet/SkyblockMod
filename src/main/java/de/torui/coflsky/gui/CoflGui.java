package de.torui.coflsky.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import de.torui.coflsky.CoflSky;
import de.torui.coflsky.CoflSkyCommand;
import gg.essential.api.utils.GuiUtil;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIWrappedText;
import gg.essential.elementa.components.inspector.Inspector;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.elementa.effects.RecursiveFadeEffect;
import gg.essential.elementa.effects.ScissorEffect;
import gg.essential.vigilance.gui.common.input.UITextInput;
import gg.essential.vigilance.gui.common.shadow.ShadowIcon;
import gg.essential.vigilance.gui.settings.SwitchComponent;
import gg.essential.vigilance.gui.settings.TextComponent;
import gg.essential.vigilance.utils.ResourceImageFactory;
import kotlin.Unit;
import net.minecraft.client.Minecraft;

public class CoflGui extends WindowScreen {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static SortedMap<String,List<JsonObject>> categories = new TreeMap<>();
    public static String selectedcategory = "general";
    public String searchQuery = "";
    public static JsonArray settings = null;
    public static JsonObject tier;
    public static String AccountStatus = "";
    @Override
	public void onScreenClose() {
        CoflGui.getSettings();
	}

    public static void getSettings() {
        System.out.println("Getting settings");
		String[] arg = "get json".split(" ");
		CoflSkyCommand.SendCommandToServer(arg, null);
        String[] args = "get tier".split(" ");
		CoflSkyCommand.SendCommandToServer(args, null);
    }

    Color clear = new Color(0,0,0,0);
    public CoflGui(Boolean doAnimation) {
        super(ElementaVersion.V2);
        reloadAllcategories();
        int screenWidth = mc.displayWidth/2;
        int screenHeight = mc.displayHeight/2;
    
        UIComponent box = new UIRoundedRectangle(8f)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.70f))
            .setHeight(new RelativeConstraint(0.70f))
            .setChildOf(getWindow())
            .setColor(new Color(0x222222))
            .enableEffect(new ScissorEffect());

        float guiWidth = box.getWidth();
        float guiHeight = box.getHeight();
        double fontScale = screenHeight/540;
        
        UIComponent titleArea = new UIBlock().setColor(clear).setChildOf(getWindow())
            .setX(new CenterConstraint())
            .setWidth(new PixelConstraint(guiWidth))
            .setHeight(new PixelConstraint(0.15f*guiHeight))
            .enableEffect(new ScissorEffect());
        // Title text
        UIComponent titleText = new UIText(ChatFormatting.BLUE+"C"+ChatFormatting.GOLD+"oflnet")
            .setChildOf(titleArea)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint((float) (doAnimation?1*fontScale:4*fontScale)));
        
        new UIText("v"+CoflSky.VERSION)
            .setColor(new Color(187,187,187))
            .setChildOf(titleArea)
            .setX(new RelativeConstraint(0.6f))
            .setY(new RelativeConstraint(0.7f))
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint((float) fontScale));

        new Inspector(getWindow()).setChildOf(getWindow());
        
        UIComponent searchBox = new UIBlock()
            .setChildOf(titleArea)
            .setX(new PixelConstraint(guiWidth-90))
            .setY(new CenterConstraint())
            .setWidth(new PixelConstraint(80))
            .setColor(new Color(120,120,120,60))
            .setHeight(new PixelConstraint(15f));

        UITextInput input = (UITextInput) new UITextInput("Search")
            .setChildOf(searchBox)
            .setX(new PixelConstraint(5f))
            .setWidth(new PixelConstraint(80))
            .setHeight(new PixelConstraint(15f))
            .setY(new PixelConstraint(3f));
        
        titleArea.onMouseClickConsumer((event)->{
            input.grabWindowFocus();;
        });
        
        // Gray horizontal line 1px from bottom of the title area
        new UIBlock().setChildOf(titleArea)
            .setWidth(new PixelConstraint(guiWidth))
            .setHeight(new PixelConstraint(1f))
            .setX(new CenterConstraint())
            .setY(new PixelConstraint(titleArea.getHeight()-1))
            .setColor(new Color(0x808080));

        UIComponent box2 = new UIBlock()
            .setX(new PixelConstraint((0.25f*guiWidth)))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setColor(new Color(0x303030))
            .enableEffect(new ScissorEffect())
            .setWidth(new PixelConstraint((0.75f*guiWidth)-10))
            .setHeight(new PixelConstraint(((0.85f*guiHeight))));

        // Area of where the currently selected catagorie's feature will be displayed
        UIComponent loadedFeaturesList = new ScrollComponent("No Matching Settings Found", 10f, Color.CYAN, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0))
            .setY(new PixelConstraint(0))
            .setChildOf(box2)
            .setColor(Color.red)
            .enableEffect(new ScissorEffect())
            .setWidth(new PixelConstraint(0.75f*guiWidth))
            .setHeight(new PixelConstraint((0.85f*guiHeight)));
        loadedFeaturesList.clearChildren();
        reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);

        input.onKeyType((component, character, integer) -> {
            searchQuery = ((UITextInput) component).getText().toLowerCase();
            loadedFeaturesList.clearChildren();
            reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);
            return Unit.INSTANCE;
        });
        
        // Side bar on the left that holds the categories
        UIComponent sidebarArea = new UIBlock()
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setWidth(new PixelConstraint(0.25f*guiWidth))
            .setHeight(new PixelConstraint(0.85f*guiHeight))
            .setChildOf(getWindow())
            .setColor(clear)
            .enableEffect(new ScissorEffect());
        int Index = 0;

        // Draw categorys on sidebar
        for(String categoryName:categories.keySet()) {
            UIComponent Examplecategory = new UIText(uppercaseFirstLetter(categoryName))
                .setChildOf(sidebarArea)
                .setColor(new Color(0xFFFFFF))
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(10f+(Index*20)))
                .enableEffect(new RecursiveFadeEffect())
                .setTextScale(new PixelConstraint((float) fontScale*2));
            if(categoryName.equals(selectedcategory)) {
                Examplecategory.setColor(new Color(0xFFAA00));
            }
            Examplecategory.onMouseEnterRunnable(()->{
                if(!categoryName.equals(selectedcategory)) Examplecategory.setColor(new Color(0xffc34d));
            });
            Examplecategory.onMouseLeaveRunnable(()->{
                if(!categoryName.equals(selectedcategory)) Examplecategory.setColor(new Color(0xFFFFFF));
            });
            Examplecategory.onMouseClickConsumer((event)->{
                selectedcategory = categoryName;
                Loadcategory(categoryName);
            });
            Index++;
        }
        Double textwidth = mc.fontRendererObj.getStringWidth("Time Left: "+ChatFormatting.YELLOW+timeTillDate(tier.get("expiresAt").getAsString()))*1.1;
        float sidebarWidth = sidebarArea.getWidth();
        float centeredX = (float) ((sidebarWidth-textwidth)/2);
        UIComponent accStatusProfileBorder = new UIRoundedRectangle(5f).setColor(new Color(getTierColor(tier.get("tier").getAsString())))
            .setChildOf(sidebarArea)
            .setX(new PixelConstraint(centeredX-1))
            .setY(new PixelConstraint((0.90f*guiHeight)-1))
            .setHeight(new PixelConstraint((0.85f*0.10f*guiHeight)+2))
            .setWidth(new PixelConstraint((textwidth.floatValue())+2));
        UIComponent accStatusProfile = new UIRoundedRectangle(5f)
            .setChildOf(sidebarArea)
            .setColor(new Color(0x303030))
            .setX(new PixelConstraint(centeredX))
            .setY(new PixelConstraint(0.90f*guiHeight))
            .setHeight(new PixelConstraint(0.85f*0.10f*guiHeight))
            .setWidth(new PixelConstraint(textwidth.floatValue()));
        new UIText("Tier: "+getAccountType(tier.get("tier").getAsString()))
            .setChildOf(accStatusProfile)
            .setTextScale(new PixelConstraint((float) ((float) fontScale*1.2)))
            .setX(new CenterConstraint())
            .setY(new RelativeConstraint(0.2f));
        new UIText("Time Left: "+ChatFormatting.YELLOW+timeTillDate(tier.get("expiresAt").getAsString()))
            .setChildOf(accStatusProfile)
            .setTextScale(new PixelConstraint((float) ((float) fontScale*1.2)))
            .setX(new CenterConstraint())
            .setY(new RelativeConstraint(0.6f));
            
        accStatusProfile.onMouseClickConsumer((event)->{
            try {
                Desktop.getDesktop().browse(new URI("https://sky.coflnet.com/premium"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });

        UIComponent discordButton = new ShadowIcon(new ResourceImageFactory("/assets/coflsky/discord.png",true),true)
            .setX(new PixelConstraint(5))
            .setWidth(new PixelConstraint(30))
            .setHeight(new PixelConstraint(30))
            .setChildOf(titleArea)
            .setY(new CenterConstraint());
        discordButton.onMouseClickConsumer((event)->{
            try {
                Desktop.getDesktop().browse(new URI("https://discord.com/invite/wvKXfTgCfb"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
        box.addChild(box2);
        box.addChild(box2);
        box.addChild(titleArea);
        box.addChild(sidebarArea);
        box.addChild(accStatusProfileBorder);
        box.addChild(accStatusProfile);
        
        if(doAnimation) {
            box.setWidth(new PixelConstraint(0f));

            AnimatingConstraints animation1 = box.makeAnimation();
            animation1.setWidthAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint(0.70f*screenWidth));
            box.animateTo(animation1);

            AnimatingConstraints animation2 = titleText.makeAnimation();
            animation2.setTextScaleAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint((float) (4.0*fontScale)));
            titleText.animateTo(animation2);
        }
    }

    static String timeTillDate(String endDate) {
        String output = "";
        if(tier.get("tier").getAsString().equals("NONE")) {
            return "Unlimited";
        }
        endDate+="Z";
        try {
            TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(endDate);
            Instant i = Instant.from(ta);
            Date d1 = new Date();
            Date d2 = Date.from(i);
            long difference_In_Time = d2.getTime() - d1.getTime();
            long difference_In_Seconds = (difference_In_Time / 1000) % 60;
            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
            long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

            if(difference_In_Years>0) output += difference_In_Years+"y ";
            if(difference_In_Days>0) output += difference_In_Days+"d ";
            if(difference_In_Hours>0) output += difference_In_Hours+"h ";
            if(difference_In_Minutes>0) output += difference_In_Minutes+"m ";
            if(difference_In_Seconds>0) output += difference_In_Seconds+"s";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public int getTierColor(String tier) {
        int color = 0x808080;
        switch (tier) {
            case "NONE":
                color = 0x808080;
                break;
            case "STARTER_PREMIUM":
                color = 0xFFFFFF;
                break;
            case "PREMIUM":
                color = 0x32de84;
                break;
            case "PREMIUM_PLUS":
                color = 0xffaa00;
                break;
            case "SUPER_PREMIUM":
                color = 0xFF5555;
                break;
            default:
                break;
        }
        return color;
    }

    public String getAccountType(String tier) {
        String status = "";
        switch (tier) {
            case "NONE":
                status = ChatFormatting.GRAY+"Free";
                break;
            case "STARTER_PREMIUM":
                status = ChatFormatting.WHITE+"Starter";
                break;
            case "PREMIUM":
                status = ChatFormatting.GREEN+"Premium";
                break;
            case "PREMIUM_PLUS":
                status = ChatFormatting.GOLD+"Premium+";
                break;
            case "SUPER_PREMIUM":
                status = ChatFormatting.RED+"Pre API";
                break;
            default:
                break;
        }
        if(status=="") {
            status=ChatFormatting.BLUE+uppercaseFirstLetter(tier);
        }
        return status;
    }

    public void reloadAllcategories() {
        categories.clear();
        for(JsonElement a:settings) {
            JsonObject setting = a.getAsJsonObject();
            if(!categories.containsKey(setting.get("category").getAsString())) {
                categories.put(setting.get("category").getAsString(), new ArrayList<JsonObject>());
            }
            categories.get(setting.get("category").getAsString()).add(setting);
        }
    }

    public void reloadFeatures(UIComponent loadedFeaturesList, float guiHeight, float guiWidth, double fontScale) {
        int index = 0; 

        // Default category
        for(String categoryName:categories.keySet()) {
            if(searchQuery.isEmpty()) {
                if(!categoryName.equals(selectedcategory)) {
                    continue;
                }
            }
            List<String> drawnSettings = new ArrayList<>();
            for(JsonObject setting:categories.get(categoryName)) {
                if(setting==null) continue;
                String name = setting.get("name").getAsString();
                String description = setting.get("info").getAsString();
                if(drawnSettings.contains(name)) continue;
                if((!formatTitle(name).toLowerCase().contains(searchQuery) && !description.toLowerCase().contains(searchQuery))) {
                    continue;
                }
                drawnSettings.add(name);
                // feature box outline:0xa9a9a9
                UIComponent exampleFeature = new UIBlock().setChildOf(loadedFeaturesList).setColor(new Color(0x444444))
                    .setX(new CenterConstraint())
                    .setY(new PixelConstraint(((5+0.15f*0.85f*guiHeight)*index)/*+20*/))
                    .setWidth(new PixelConstraint(0.90f*0.75f*guiWidth))
                    .setHeight(new PixelConstraint(0.15f*0.85f*guiHeight))
                    .enableEffect(new OutlineEffect(new Color(0xa9a9a9),1f));
    
                // Feature Title
                new UIText(formatTitle(name)).setChildOf(exampleFeature)
                    .setY(new PixelConstraint(4f))
                    .setX(new PixelConstraint(4f))
                    .setTextScale(new PixelConstraint((float) fontScale*2f));
    
                new UIWrappedText(uppercaseFirstLetter(description)).setChildOf(exampleFeature)
                    .setX(new PixelConstraint(4f))
                    .setWidth(new PixelConstraint(350))
                    .setColor(new Color(187,187,187))
                    .setY(new PixelConstraint(23f*(float) fontScale))
                    .setTextScale(new PixelConstraint((float) fontScale*1f));
            
                
                String type = setting.get("type").getAsString();
                if(type.equals("Boolean")) {
                    UIComponent comp = new SwitchComponent(setting.get("value").getAsBoolean()).setChildOf(exampleFeature);
                    ((SwitchComponent) comp).onValueChange((value)->{
                        if(value!=setting.get("value")) {
                            updateSetting(setting.get("key").getAsString(),value);
                        }
                        return Unit.INSTANCE;
                    });
                }

                if(type.equals("String")) {
                    if(setting.get("value")==null) continue;
                    String formattedValue = "";
                    try {formattedValue = setting.get("value").getAsString();} catch (Exception e) {continue;}
                    formattedValue = formattedValue.replaceAll("\\\"","");
                    formattedValue = formattedValue.replaceAll("§", "&");
                    UIComponent comp = new TextComponent(formattedValue, "", false, false).setChildOf(exampleFeature);
                    ((TextComponent) comp).onValueChange((value)->{
                        if(!value.toString().equals(setting.get("value").toString()) && value.toString().length()>0) {
                            value = ((String) value).replaceAll("&", "§");
                            updateSetting(setting.get("key").getAsString(),value);
                        }
                        return Unit.INSTANCE;
                    });
                }
    
                if(type.equals("Int64") || type.equals("Double") || type.equals("Int32")) {
                    UIComponent comp = new TextComponent(setting.get("value")+"", "", false, false).setChildOf(exampleFeature);
                    ((TextComponent) comp).onValueChange((value)->{
                        value=value.toString().replaceAll("[^0-9m.btk]","");
                        if(!value.toString().equals(setting.get("value").toString()) && value.toString().length()>0) {
                            System.out.println("DIFFERENCE '"+value+"' '"+setting.get("value")+"'");
                            updateSetting(setting.get("key").getAsString(),value);
                        }
                        return Unit.INSTANCE;
                    });
                }
                index++;
            }
        }
    }

    public void updateSetting(String settingName,Object value) {
        if(value.toString().isEmpty()) return;
		String[] arg = ("set "+settingName+" "+value).split(" ");
		CoflSkyCommand.SendCommandToServer(arg, null);
    }

    public String uppercaseFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String formatTitle(String str) {
        String[] r = str.split("(?=\\p{Lu})");
        String output = "";
        for(int i=0;i<r.length;i++) {
            output+=r[i]+" ";
        }
        return output;
    }

    public void Loadcategory(String categoryName) {
        GuiUtil.open(new CoflGui(false));
    }
}