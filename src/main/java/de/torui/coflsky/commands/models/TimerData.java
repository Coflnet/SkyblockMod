package de.torui.coflsky.commands.models;

import com.google.gson.annotations.SerializedName;

public class TimerData {
	
	@SerializedName("seconds")
	public double seconds;
	
	@SerializedName("heightPercent")
	public int height;
	@SerializedName("widthPercent")
	public int width;
	@SerializedName("scale")
	public double scale;
	
	@SerializedName("prefix")
	public String prefix;

	@SerializedName("maxPrecision")
	public int maxPrecision;
}
