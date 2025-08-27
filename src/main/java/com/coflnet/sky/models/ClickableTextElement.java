package com.coflnet.sky.models;

/**
 * Represents a clickable text element with hover functionality for the info display
 */
public class ClickableTextElement {
    private String text;
    private String hover;
    private String onClick;

    public ClickableTextElement() {
    }

    public ClickableTextElement(String text, String hover, String onClick) {
        this.text = text;
        this.hover = hover;
        this.onClick = onClick;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHover() {
        return hover;
    }

    public void setHover(String hover) {
        this.hover = hover;
    }

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }
}
