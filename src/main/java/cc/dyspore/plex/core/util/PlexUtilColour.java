package cc.dyspore.plex.core.util;

import net.minecraft.client.Minecraft;

import java.util.Arrays;

public class PlexUtilColour {
    public static int fromRGB(int r, int g, int b, int a) {
        return ((r & 255) << 16) | ((g & 255) << 8) | (b & 255) | ((a & 255) << 24);
    }

    public static int channel(int colour, int channel) {
        return (colour >> (channel != 3 ? 16 - (channel * 8) : 24)) & 255;
    }

    private static int betweenChannel(int number1, int number2, float between) {
        return (int) (number1 + (number2 - number1) * between);
    }

    public static int between(int colour1, int colour2, float between) {
        between = PlexUtil.clamp(between, 0.0F, 1.0F);
        return fromRGB(
                betweenChannel(channel(colour1, 0), channel(colour2, 0), between),
                betweenChannel(channel(colour1, 1), channel(colour2, 1), between),
                betweenChannel(channel(colour1, 2), channel(colour2, 2), between),
                betweenChannel(channel(colour1, 3), channel(colour2, 3), between));
    }

    private static int replaceChannel(int colour, int replacement) {
        return replacement != -1 ? replacement : colour;
    }

    public static int replace(int colour, int r, int g, int b, int a) {
        return fromRGB(
                replaceChannel(channel(colour, 0), r),
                replaceChannel(channel(colour, 1), g),
                replaceChannel(channel(colour, 2), b),
                replaceChannel(channel(colour, 3), a));
    }

    public static int setAlpha(int colour, int alpha) {
        return (colour & 0xffffff) | ((alpha & 255) << 24);
    }

    public static int chroma(double i) {
        int i2 = (int) (i % 1531);
        return chroma(i2);
    }

    public static int chroma(int i) {
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
        if (i > 1020) {
            blue = i <= 1275 ? 255 : (1530 - i);
            red = i <= 1275 ? i - 1020 : 255;
        }
        return fromRGB(red, green, blue, 255);
    }

    public static int globalChromaCycle() {
        return chroma((Minecraft.getSystemTime() / 100.0D * 20.0D));
    }

    public static int multiply(int colour, float mul) {
        return multiply(colour, mul, false);
    }

    public static int multiply(int colour, float mul, boolean alpha) {
        return multiply(colour, mul, mul, mul, alpha ? mul : 1.0F);
    }

    public static int multiply(int colour, float r, float g, float b, float a) {
        return fromRGB(
                multiplyChannel(channel(colour, 0), r),
                multiplyChannel(channel(colour, 1), g),
                multiplyChannel(channel(colour, 2), b),
                multiplyChannel(channel(colour, 3), a));
    }

    private static int multiplyChannel(int channelValue, float multiplier) {
        multiplier = PlexUtil.clamp(multiplier, 0.0F, 2.0F);
        return (int) (multiplier > 1.0 ? channelValue + ((255 - channelValue) * (multiplier - 1.0F)) : channelValue * multiplier);
    }

    public enum PaletteState {
        FIXED,
        CHROMA
    }

    public static class ColourPalette {
        public int[] colours;
        public PaletteState[] states;
        public long[] timings;

        public long transitionTime;

        public int[] previousColours; // no reference given to previous objects to prevent a chain of references
        public PaletteState[] previousStates;

        private ColourPalette(int[] previousColours, PaletteState[] previousStates, long transitionTime) {
            this.previousColours = previousColours;
            this.previousStates = previousStates;
            this.setup(transitionTime, 0L);
        }

        public ColourPalette(ColourPalette previous, int transitionTime) {
            this(previous.getActiveColours(), previous.states.clone(), transitionTime);
        }

        public ColourPalette(int slots, int defaultColour, PaletteState defaultState, long transitionTime) {
            this.previousColours = new int[slots];
            this.previousStates = new PaletteState[slots];
            Arrays.fill(previousColours, defaultColour);
            Arrays.fill(previousStates, defaultState);
            this.setup(transitionTime, -transitionTime);
        }

        private void setup(long transitionTime, long timingsDelta) {
            this.colours = previousColours.clone();
            this.states = previousStates.clone();
            this.transitionTime = transitionTime;
            long timingsStart = Minecraft.getSystemTime() + timingsDelta;
            this.timings = new long[this.colours.length];
            Arrays.fill(this.timings, timingsStart);
        }

        public int[] getActiveColours() {
            int[] activeColours = new int[this.colours.length];
            for (int i = 0; i < this.colours.length; i++) {
                activeColours[i] = this.getActiveColour(i);
            }
            return activeColours;
        }

        private int statedColour(int colour, PaletteState state) {
            switch (state) {
                case CHROMA:
                    return globalChromaCycle();
                default:
                    return colour;
            }
        }

        public int getActiveColour(int slot) {
            return between(
                    statedColour(this.previousColours[slot], this.previousStates[slot]),
                    statedColour(this.colours[slot], this.states[slot]),
                    (float) ((double) PlexUtil.clamp(Minecraft.getSystemTime() - this.timings[slot], 0, this.transitionTime) / this.transitionTime));
        }

        public void setColour(int slot, int colour, PaletteState state, boolean force) {
            if (this.states[slot] == null) {
                force = true;
            }
            if (force || this.colours[slot] != colour || this.states[slot] != state) {
                this.previousColours[slot] = force ? colour : this.getActiveColour(slot);
                this.previousStates[slot] = force ? state : PaletteState.FIXED;
                this.colours[slot] = colour;
                this.states[slot] = state;
                this.timings[slot] = Minecraft.getSystemTime() - (force ? this.transitionTime : 0);
            }
        }

        public void setColour(int slot, int colour, PaletteState state) {
            this.setColour(slot, colour, state, false);
        }
    }
}
