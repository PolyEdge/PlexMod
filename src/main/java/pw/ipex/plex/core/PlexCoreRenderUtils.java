package pw.ipex.plex.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import pw.ipex.plex.Plex;

public class PlexCoreRenderUtils extends GuiScreen {
	public PlexCoreRenderUtils() {
		this.fontRendererObj = Plex.minecraft.fontRendererObj;
	}

	public void drawScaledString(String text, float x, float y, int colour, float scale, boolean shadow) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		Plex.minecraft.fontRendererObj.drawString(text, x / scale, y / scale, colour, shadow);
		GL11.glPopMatrix();		
	}

	public void drawScaledStringRightSide(String text, int x, int y, int colour, float scale, boolean shadow) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		Plex.minecraft.fontRendererObj.drawString(text, x / scale - Plex.minecraft.fontRendererObj.getStringWidth(text), y, colour, shadow);
		GL11.glPopMatrix();
	}

	public void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale, boolean shadow) {
		drawScaledString(text, x, y, colour, scale, shadow);
	}

	public void drawScaledStringRightSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringRightSide(text, x, y, colour, scale, false);
	}

	public void drawScaledStringLeftSide(String text, int x, int y, int colour, float scale) {
		drawScaledStringLeftSide(text, x, y, colour, scale, false);
	}

	public int calculateScaledStringWidth(String text, float scale) {
		return (int)(Plex.minecraft.fontRendererObj.getStringWidth(text) * scale);
	}

	public int calculateScaledStringHeight(float scale) {
		return (int) (scale * Plex.minecraft.fontRendererObj.FONT_HEIGHT);
	}
	
	public String trimScaledTextToWidth(String text, int width, float scale) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		String outputText = Plex.minecraft.fontRendererObj.trimStringToWidth(text, (int) (width * scale));
		GL11.glPopMatrix();
		return outputText;
	}
	
	public List<String> textWrapScaledString(String text, int width, float scale) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		List<String> outputText = Plex.minecraft.fontRendererObj.listFormattedStringToWidth(text, width);
		GL11.glPopMatrix();	
		return outputText;
	}

	public int drawCenteredTextWrapScaledString(String text, int x, int y, int width, float scale, int colour, int lineSpacing) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		List<String> outputText = Plex.minecraft.fontRendererObj.listFormattedStringToWidth(text, width);
		int maxWidth = 0;
		for (String line : outputText) {
			int length = Plex.minecraft.fontRendererObj.getStringWidth(line);
			if (length > maxWidth) {
				maxWidth = length;
			}
		}
		int startX = (int) ((x / scale) - (maxWidth / 2));
		int yPos = 0;
		for (String line : outputText) {
			Plex.minecraft.fontRendererObj.drawString(line, startX, (int) (y / scale) + yPos, colour);
			yPos += Plex.minecraft.fontRendererObj.FONT_HEIGHT + lineSpacing;
		}
		GL11.glPopMatrix();
		return yPos;
	}

	public int calculateCenteredTextWrapScaledStringHeight(String text, int width, float scale, int lineSpacing) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		List<String> outputText = Plex.minecraft.fontRendererObj.listFormattedStringToWidth(text, width);
		int maxWidth = 0;
		for (String line : outputText) {
			int length = Plex.minecraft.fontRendererObj.getStringWidth(line);
			if (length > maxWidth) {
				maxWidth = length;
			}
		}
		int yPos = 0;
		for (String line : outputText) {
			yPos += Plex.minecraft.fontRendererObj.FONT_HEIGHT + lineSpacing;
		}
		GL11.glPopMatrix();
		return yPos;
	}

	public void drawScaledXImage(ResourceLocation image, int x, int y, int width, float offsetX, float offsetY) {
		Plex.minecraft.renderEngine.bindTexture(image);
		//GuiScreen.drawScaledCustomSizeModalRect(x, y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
	}
	
	public void drawPlayerHead(String username, int x, int y, int size) {
		drawPlayerHead(PlexCoreUtils.getSkin(username), x, y, size);
	}

	public void drawPlayerHead(ResourceLocation playerSkin, int x, int y, int size) {
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

	public void drawGradientRect(int startX, int startY, int endX, int endY, int zLevel, int startColor, int endColor) {
		float sa = (float)(startColor >> 24 & 255) / 255.0F;
		float sr = (float)(startColor >> 16 & 255) / 255.0F;
		float sg = (float)(startColor >> 8 & 255) / 255.0F;
		float sb = (float)(startColor & 255) / 255.0F;
		float ea = (float)(endColor >> 24 & 255) / 255.0F;
		float er = (float)(endColor >> 16 & 255) / 255.0F;
		float eg = (float)(endColor >> 8 & 255) / 255.0F;
		float eb = (float)(endColor & 255) / 255.0F;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos((double)endX, (double)startY, (double)zLevel).color(sr, sg, sb, sa).endVertex();
		worldrenderer.pos((double)startX, (double)startY, (double)zLevel).color(sr, sg, sb, sa).endVertex();
		worldrenderer.pos((double)startX, (double)endY, (double)zLevel).color(er, eg, eb, ea).endVertex();
		worldrenderer.pos((double)endX, (double)endY, (double)zLevel).color(er, eg, eb, ea).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();

	}

	public void drawScaledGradientRect(int startX, int startY, int endX, int endY, float scale, int colour1, int colour2) {
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		this.drawGradientRect((int) (startX / scale), (int) (startY / scale), (int) (endX / scale), (int) (endY / scale), colour1, colour2);
		GL11.glPopMatrix();
	}

	public void drawScaledHorizontalLine(int startX, int endX, int y, float scale, int color) {
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

	public void drawScaledVerticalLine(int x, int startY, int endY, float scale, int color) {
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

	public void staticDrawTooltip(String text, int x, int y) {
		List<String> textLines = new ArrayList<>();
		for (String line : text.split("\n")) {
			if (line != null) {
				textLines.add(line);
			}
		}
		this.staticDrawTooltip(textLines, x, y, -1, Plex.minecraft.fontRendererObj);
		//this.drawHoveringText(textLines, x, y, Plex.minecraft.fontRendererObj);
	}

	public void staticDrawTooltip(List<String> textLines, int x, int y, int maxWidth, FontRenderer fontRenderer) {
		int displayWidth = Plex.minecraft.displayWidth;
		int displayHeight = Plex.minecraft.displayHeight;
		int zPosition = 300;

		GlStateManager.enableDepth();
		GlStateManager.disableRescaleNormal();

		int tooltipTextWidth = 0;

		for (String textLine : textLines) {
			int textLineWidth = fontRenderer.getStringWidth(textLine);

			if (textLineWidth > tooltipTextWidth) {
				tooltipTextWidth = textLineWidth;
			}
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = x + 12;
		if (tooltipX + tooltipTextWidth + 4 > displayWidth) {
			tooltipX = x - 16 - tooltipTextWidth;
			if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
				if (x > displayWidth / 2) {
					tooltipTextWidth = x - 12 - 8;
				}
				else {
					tooltipTextWidth = displayWidth - 16 - x;
				}
				needsWrap = true;
			}
		}

		if (maxWidth > 0 && tooltipTextWidth > maxWidth) {
			tooltipTextWidth = maxWidth;
			needsWrap = true;
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<String> wrappedTextLines = new ArrayList<String>();
			for (int i = 0; i < textLines.size(); i++) {
				String textLine = textLines.get(i);
				List<String> wrappedLine = fontRenderer.listFormattedStringToWidth(textLine, tooltipTextWidth);
				if (i == 0) {
					titleLinesCount = wrappedLine.size();
				}

				for (String line : wrappedLine) {
					int lineWidth = fontRenderer.getStringWidth(line);
					if (lineWidth > wrappedTooltipWidth) {
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (x > displayWidth / 2) {
				tooltipX = x - 16 - tooltipTextWidth;
			}
			else {
				tooltipX = x + 12;
			}
		}

		int tooltipY = y - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1) {
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount) {
				tooltipHeight += 2; // gap between title lines and next lines
			}
		}

		if (tooltipY + tooltipHeight + 6 > displayHeight) {
			tooltipY = displayHeight - tooltipHeight - 6;
		}

		int backgroundColor = 0xF0100010;
		this.drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, zPosition, backgroundColor, backgroundColor);
		this.drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, zPosition, backgroundColor, backgroundColor);
		this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, zPosition, backgroundColor, backgroundColor);
		this.drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, zPosition, backgroundColor, backgroundColor);
		this.drawGradientRect(tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, zPosition, backgroundColor, backgroundColor);
		int borderColorStart = 0x505000FF;
		int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
		this.drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, zPosition, borderColorStart, borderColorEnd);
		this.drawGradientRect(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, zPosition, borderColorStart, borderColorEnd);
		this.drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, zPosition, borderColorStart, borderColorStart);
		this.drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, zPosition, borderColorEnd, borderColorEnd);

		GlStateManager.disableDepth();
		for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
			String line = textLines.get(lineNumber);
			fontRenderer.drawStringWithShadow(line, (float) tooltipX, (float) tooltipY, -1);

			if (lineNumber + 1 == titleLinesCount) {
				tooltipY += 2;
			}

			tooltipY += 10;
		}

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableDepth();
	}

	public void staticDrawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		this.drawGradientRect(left, top, right, bottom, startColor, endColor);
	}
}
