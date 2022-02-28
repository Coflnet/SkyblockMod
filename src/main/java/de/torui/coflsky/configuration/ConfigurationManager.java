package de.torui.coflsky.configuration;

import java.lang.reflect.Field;

import de.torui.coflsky.network.WSClient;
import net.minecraft.client.Minecraft;
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
		
		if(newConfig ==null)
		{
			System.out.println("Could not deserialize configuration "+ data);
		}
		
		
		try {
			if(CompareProperties(Config, newConfig)) {
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
					UpdatedProperty(f);
					updatedProperties++;
				}
				break;
			case "boolean":
				if (f.getBoolean(old) != f.getBoolean(newConfiguration)) {
					UpdatedProperty(f);
					updatedProperties++;
				}
				break;
			case "java.lang.String":
				if (f.get(old) != null && !f.get(old).equals(f.get(newConfiguration))) {
					UpdatedProperty(f);
					updatedProperties++;
				}
				break;

			default:
				throw new RuntimeException("Invalid Configuration Type " + f.getGenericType().getTypeName());
			}

		}

		return updatedProperties > 0;
	}

	public void UpdatedProperty(Field propertyName) {
		Description description = propertyName.getAnnotation(Description.class);
		IChatComponent comp;
		if (description != null) {

			comp = new ChatComponentText("The Configuration Setting ")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE))
					.appendSibling(new ChatComponentText(description.value())
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
					.appendSibling(new ChatComponentText(" has been updated")
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE))));
		} else {
			comp = new ChatComponentText("The Configuration Setting ")
					.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE))
					.appendSibling(new ChatComponentText(propertyName.getName())
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
					.appendSibling(new ChatComponentText(" has been updated")
							.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE))));
			
			
			System.out.println("Field " + propertyName.getName() + " has no description!");
		}
		
		Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
	}

}
