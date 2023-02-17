package de.torui.coflsky.gui.bingui.helper;

import java.awt.*;

public enum ColorPallet {
    //could be changed through a config or something
    PRIMARY(new Color(34, 40, 49, 255)),
    SECONDARY(new Color(57, 62, 70, 255)),
    TERTIARY(new Color(0, 173, 181, 255)),
    WHITE(new Color(238, 238, 238, 255)),
    ERROR(new Color(178, 30, 30, 255)),
    SUCCESS(new Color(114, 208, 0, 255)),
    WARNING(new Color(208, 121, 22, 255)),
    INFO(new Color(204, 253, 233, 255));

    private Color color;

    ColorPallet(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
