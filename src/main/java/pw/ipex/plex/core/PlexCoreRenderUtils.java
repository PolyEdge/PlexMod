package pw.ipex.plex.core;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import pw.ipex.plex.Plex;

public class PlexCoreRenderUtils extends GuiScreen {
	public static PlexCoreRenderUtils renderInstance;

	static {
		renderInstance = new PlexCoreRenderUtils();
	}

	public static void drawScaledString(String text, float x, float y, int colour, float scale, boolean shadow) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		Plex.minecraft.fontRendererObj.drawString(text, x / scale, y / scale, colour, shadow);
		GL11.glPopMatrix();		
	}

	public static void drawScaledStringRightSide(String text, int x, int y, int colour, float scale, boolean shadow) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		Plex.minecraft.fontRendererObj.drawString(text, x / scale - Plex.minecraft.fontRendererObj.getStringWidth(text), y, colour, shadow);
		GL11.glPopMatrix();
	}

	public static void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale, boolean shadow) {
		drawScaledString(text, x, y, colour, scale, shadow);
	}

	public static void drawScaledStringRightSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringRightSide(text, x, y, colour, scale, false);
	}

	public static void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringLeftSide(text, x, y, colour, scale, false);
	}

	public static int calculateScaledStringWidth(String text, float scale) {
		return (int)(Plex.minecraft.fontRendererObj.getStringWidth(text) * scale);
	}
	
	public static String trimScaledTextToWidth(String text, int width, float scale) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		String outputText = Plex.minecraft.fontRendererObj.trimStringToWidth(text, (int) (width * scale));
		GL11.glPopMatrix();
		return outputText;
	}
	
	public static List<String> textWrapScaledString(String text, int width, float scale) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		List<String> outputText = Plex.minecraft.fontRendererObj.listFormattedStringToWidth(text, width);
		GL11.glPopMatrix();	
		return outputText;
	}

	public static void drawScaledXImage(ResourceLocation image, int x, int y, int width, float offsetX, float offsetY) {
		Plex.minecraft.renderEngine.bindTexture(image);
		//GuiScreen.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
	}
	
	public static void drawPlayerHead(String username, int x, int y, int size) {
		PlexCoreRenderUtils.drawPlayerHead(PlexCoreUtils.getSkin(username), x, y, size);
	}

	public static void drawPlayerHead(ResourceLocation playerSkin, int x, int y, int size) {
		Plex.minecraft.renderEngine.bindTexture(playerSkin);
		GL11.glPushMatrix();
		GlStateManager.disableDepth();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableCull();
		GuiScreen.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
		GuiScreen.drawScaledCustomSizeModalRect(x, y, 40.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
		GL11.glPopMatrix();
	}

	public static void drawScaledGradientRect(int startX, int startY, int endX, int endY, float scale, int colour1, int colour2) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		renderInstance.drawGradientRect((int) (startX / scale), (int) (startY / scale), (int) (endX / scale), (int) (endY / scale), colour1, colour2);
		GL11.glPopMatrix();
	}

	public static void drawScaledHorizontalLine(int startX, int endX, int y, float scale, int color) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		if (endX < startX) {
			int i = startX;
			startX = endX;
			endX = i;
		}
		drawRect((int) (startX / scale), (int) (y / scale), (int) ((endX + 1) / scale), (int) ((y + 1) / scale), color);
		GL11.glPopMatrix();
	}

	public static void drawScaledVerticalLine(int x, int startY, int endY, float scale, int color) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		if (endY < startY) {
			int i = startY;
			startY = endY;
			endY = i;
		}
		drawRect((int) (x / scale), (int) ((startY + 1) / scale), (int) ((x + 1) / scale), (int) (endY / scale), color);
		GL11.glPopMatrix();
	}

	public static void staticDrawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		renderInstance.drawGradientRect(left, top, right, bottom, startColor, endColor);
	}
}
