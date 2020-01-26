package cc.dyspore.plex.core.util;

import net.minecraft.client.Minecraft;

public class PlexUtilColour {
    public static int fromRGB(int r, int g, int b, int a) {
        r = PlexUtil.clamp(r, 0, 255);
        g = PlexUtil.clamp(g, 0, 255);
        b = PlexUtil.clamp(b, 0, 255);
        a = PlexUtil.clamp(a, 0, 255);
        return (r << 16) + (g << 8) + (b) + (a << 24);
    }

    public static int[] toRGB(int colour) {
        return new int[] {((colour >> 16) & 255), ((colour >> 8) & 255), (colour & 255), ((colour >> 24) & 255)};
    }

    public static int between(int colour1, int colour2, float between) {
        between = PlexUtil.clamp(between, 0.0F, 1.0F);
        int[] rgb1 = toRGB(colour1);
        int[] rgb2 = toRGB(colour2);
        int colourR = rgb1[0] + ((int) (between * (rgb2[0] - rgb1[0])));
        int colourG = rgb1[1] + ((int) (between * (rgb2[1] - rgb1[1])));
        int colourB = rgb1[2] + ((int) (between * (rgb2[2] - rgb1[2])));
        int colourA = rgb1[3] + ((int) (between * (rgb2[3] - rgb1[3])));
        return fromRGB(colourR, colourG, colourB, colourA);
    }

    public static int replace(int colour, Integer r, Integer g, Integer b, Integer a) {
        int[] rgba = toRGB(colour);
        return fromRGB(r == null ? rgba[0] : r, g == null ? rgba[1] : g, b == null ? rgba[2] : b, a == null ? rgba[3] : a);
    }

    public static int[] chroma(double i) {
        int i2 = (int) (i % 1531);
        return chroma(i2);
    }

    public static int[] chroma(int i) {
        i = i % 1531;
        int red = 0;
        int green = 0;
        int blue = 0;
        if (i <= 510) {
            red = i <= 255 ? 255 : (510 - i);
            green = i <= 255 ? i : 255;
        }
        if ((i > 510) && (i <= 1020)) {
            green = i <= 765 ? 255 : (1020 - i);
            blue = i <= 765 ? i - 510 : 255;
        }
        if ((i > 1020) && (i <= 1530)) {
            blue = i <= 1275 ? 255 : (1530 - i);
            red = i <= 1275 ? i - 1020 : 255;
        }
        return new int[] {red, green, blue};
    }

    public static int globalChromaCycle() {
        int[] chromaRGB = chroma((Minecraft.getSystemTime() / 100.0D * 20.0D));
        return fromRGB(chromaRGB[0], chromaRGB[1], chromaRGB[2], 255);
    }

    public static int multiply(int colour, float mul) {
        return multiply(colour, mul, false);
    }

    public static int multiply(int colour, float mul, boolean alpha) {
        int[] rgb = toRGB(colour);
        return fromRGB((int) (mul * rgb[0]), (int) (mul * rgb[1]), (int) (mul * rgb[2]), alpha ? (int) (mul * rgb[3]) : rgb[3]);
    }

    public static int multiply(int colour, float r, float g, float b, float a) {
        int[] rgb = toRGB(colour);
        return fromRGB((int) (r * rgb[0]), (int) (g * rgb[1]), (int) (b * rgb[2]), (int) (a * rgb[3]));
    }
}
