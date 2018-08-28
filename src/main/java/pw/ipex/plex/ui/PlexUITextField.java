package pw.ipex.plex.ui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

public class PlexUITextField extends net.minecraft.client.gui.Gui {
	public GuiTextField text;
	public Integer id;
	public FontRenderer fontRendererInstance;
	public Integer xPosition;
	public Integer yPosition;
	public Integer itemWidth;
	public Integer itemHeight;
	 
	public PlexUITextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
		this.id = componentId;
		this.fontRendererInstance = fontrendererObj;
		this.xPosition = x;
		this.yPosition = y;
		this.itemWidth = par5Width;
		this.itemHeight = par6Height;
		this.text = new GuiTextField(this.id, this.fontRendererInstance, this.xPosition, this.yPosition, this.itemWidth, this.itemHeight);
	}
	 
	public void keyTyped(char par1, int par2) {
		this.text.textboxKeyTyped(par1, par2);
	} 

	public void updateScreen() {
		this.text.updateCursorCounter();
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.text.drawTextBox();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
		//Plex.logger.info("test");
	}


	public void mouseClicked(int x, int y, int btn) {
		this.text.mouseClicked(x, y, btn);
	}
}
