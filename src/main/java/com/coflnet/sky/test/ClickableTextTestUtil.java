package com.coflnet.sky.test;

import com.coflnet.sky.models.ClickableTextElement;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for testing clickable text functionality
 */
public class ClickableTextTestUtil {
    
    public static void main(String[] args) {
        // Test the JSON generation for clickable text elements
        testClickableTextGeneration();
    }
    
    public static void testClickableTextGeneration() {
        List<ClickableTextElement> elements = new ArrayList<>();
        
        // Add first element - main clickable text
        ClickableTextElement element1 = new ClickableTextElement();
        element1.setText("Test Text");
        element1.setHover("Test hover\n§bline 2");
        element1.setOnClick("/cofl help");
        elements.add(element1);
        
        // Add second element - another clickable text
        ClickableTextElement element2 = new ClickableTextElement();
        element2.setText(" O");
        element2.setHover("Test hover");
        element2.setOnClick("/cofl dialog echo hi");
        elements.add(element2);
        
        // Convert to JSON
        Gson gson = new Gson();
        String json = gson.toJson(elements);
        
        System.out.println("Generated JSON for clickable text:");
        System.out.println(json);
        
        // Test parsing back
        System.out.println("\nTesting parsing back:");
        try {
            com.google.gson.reflect.TypeToken<List<ClickableTextElement>> typeToken = 
                new com.google.gson.reflect.TypeToken<List<ClickableTextElement>>(){};
            List<ClickableTextElement> parsed = gson.fromJson(json, typeToken.getType());
            
            for (int i = 0; i < parsed.size(); i++) {
                ClickableTextElement elem = parsed.get(i);
                System.out.println("Element " + i + ":");
                System.out.println("  Text: " + elem.getText());
                System.out.println("  Hover: " + elem.getHover());
                System.out.println("  OnClick: " + elem.getOnClick());
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }
    
    /**
     * Generate sample JSON strings for testing
     */
    public static String generateSampleJson1() {
        List<ClickableTextElement> elements = new ArrayList<>();
        elements.add(new ClickableTextElement("Click for help", "Opens the help dialog", "/cofl help"));
        return new Gson().toJson(elements);
    }
    
    public static String generateSampleJson2() {
        List<ClickableTextElement> elements = new ArrayList<>();
        elements.add(new ClickableTextElement("Buy Item", "Click to purchase this item\n§aCost: 1000 coins", "/cofl buy"));
        elements.add(new ClickableTextElement(" | ", null, null));
        elements.add(new ClickableTextElement("View Auction", "Click to view the auction\n§eAuction ID: abc123", "/cofl viewauction abc123"));
        return new Gson().toJson(elements);
    }
    
    public static String generateSampleJson3() {
        List<ClickableTextElement> elements = new ArrayList<>();
        elements.add(new ClickableTextElement("Status: ", null, null)); // Non-clickable text
        elements.add(new ClickableTextElement("Active", "System is running normally\n§aLast update: Now", "/cofl status"));
        return new Gson().toJson(elements);
    }
}
