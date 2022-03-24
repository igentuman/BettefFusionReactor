package igentuman.bfr.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import igentuman.bfr.common.tile.reactor.TileEntityReactorLogicAdapter;
import igentuman.bfr.common.tile.reactor.TileEntityReactorLogicAdapter.ReactorLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReactorLogicButton extends GuiButton {

    @Nonnull
    private final TileEntityReactorLogicAdapter tile;
    private final ResourceLocation resourceLocation;
    private final ReactorLogic type;

    public GuiReactorLogicButton(int id, int x, int y, ReactorLogic type, @Nonnull TileEntityReactorLogicAdapter tile, ResourceLocation resource) {
        super(id, x, y, 128, 22, "");
        this.tile = tile;
        this.type = type;
        this.resourceLocation = resource;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(this.resourceLocation);
            MekanismRenderer.color(EnumColor.RED);
            drawTexturedModalRect(this.x, this.y, 0, 166 + (type == tile.logicType ? 22 : 0), this.width, this.height);
            MekanismRenderer.resetColor();
        }
    }

    public ReactorLogic getType() {
        return this.type;
    }
}