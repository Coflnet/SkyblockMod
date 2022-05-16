package de.torui.coflsky.configuration;

import java.lang.reflect.Field;
import java.util.Arrays;

import de.torui.coflsky.network.WSClient;
import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ConfigurationManager {

    public Configuration Config;

    public ConfigurationManager() {
        this.Config = Configuration.getInstance();
    }

    public void UpdateConfiguration(String data) {

        Configuration newConfig = WSClient.gson.fromJson(data, Configuration.class);

        if (newConfig == null) {
            System.out.println("Could not deserialize configuration " + data);
        }


        try {
            if (CompareProperties(Config, newConfig)) {
                Configuration.setInstance(newConfig);
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean CompareProperties(Configuration old, Configuration newConfiguration)
            throws IllegalArgumentException, IllegalAccessException {

        int updatedProperties = 0;
        for (Field f : Configuration.class.getFields()) {

            switch (f.getGenericType().getTypeName()) {

                case "int":
                    if (f.getInt(old) != f.getInt(newConfiguration)) {
                        UpdatedProperty(f,newConfiguration);
                        updatedProperties++;
                    }
                    break;
                case "boolean":
                    if (f.getBoolean(old) != f.getBoolean(newConfiguration)) {
                        UpdatedProperty(f,newConfiguration);
                        updatedProperties++;
                    }
                    break;
                case "java.lang.String":

                    if (f.get(old) != null && !f.get(old).equals(f.get(newConfiguration))) {
                        UpdatedProperty(f,newConfiguration);
                        updatedProperties++;
                    }
                    break;
                case "java.lang.String[]":
                    if (!Arrays.deepEquals((String[]) f.get(old), (String[]) f.get(newConfiguration))) {
                        UpdatedProperty(f,newConfiguration);
                        updatedProperties++;
                    }
                    break;

                default:
                    throw new RuntimeException("Invalid Configuration Type " + f.getGenericType().getTypeName());
            }

        }

        return updatedProperties > 0;
    }

    private IChatComponent GetNameFormatted(Field propertyName) {
        Description description = propertyName.getAnnotation(Description.class);
        ChatComponentText toReturn = new ChatComponentText(propertyName.getName());

        ChatStyle style = new ChatStyle();
        style.setColor(EnumChatFormatting.WHITE);

        if (description != null) {
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText(description.value())));
        }

        return toReturn.setChatStyle(style);

    }

    public void UpdatedProperty(Field propertyName,Configuration confignew) throws IllegalAccessException {
        System.out.println("The Configuration Setting " + propertyName.getName() + " has been updated to " + propertyName.get(confignew));
    }

}
