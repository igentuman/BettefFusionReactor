package igentuman.bfr.client.render;

import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import igentuman.bfr.common.tile.reactor.TileEntityReactorController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderReactor extends TileEntitySpecialRenderer<TileEntityReactorController> {

    private ModelEnergyCore core = new ModelEnergyCore();

    @Override
    public void render(TileEntityReactorController tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.isBurning()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.5F, (float) y - 1.5F, (float) z + 0.5F);
            bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();

            long scaledTemp = Math.round(tileEntity.getPlasmaTemp() / 1E8);
            float ticks = MekanismClient.ticksPassed + partialTick;
            double scale = 1 + 0.7 * Math.sin(Math.toRadians(ticks * 3.14 * scaledTemp + 135F));
            renderPart(EnumColor.AQUA, scale, ticks, scaledTemp, -6, -7, 0, 36);

            scale = 1 + 0.8 * Math.sin(Math.toRadians(ticks * 3 * scaledTemp));
            renderPart(EnumColor.RED, scale, ticks, scaledTemp, 4, 4, 0, 36);

            scale = 1 - 0.9 * Math.sin(Math.toRadians(ticks * 4 * scaledTemp + 90F));
            renderPart(EnumColor.ORANGE, scale, ticks, scaledTemp, 5, -3, -35, 106);

            MekanismRenderer.disableGlow(glowInfo);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
    }

    private void renderPart(EnumColor color, double scale, float ticks, long scaledTemp, int mult1, int mult2, int shift1, int shift2) {
        float ticksScaledTemp = ticks * scaledTemp;
        GlStateManager.pushMatrix();
        GlStateManager.scale((float) scale, (float) scale, (float) scale);
        MekanismRenderer.color(color);
        GlStateManager.rotate(ticksScaledTemp * mult1 + shift1, 0, 1, 0);
        GlStateManager.rotate(ticksScaledTemp * mult2 + shift2, 0, 1, 1);
        core.render(0.0625F);
        MekanismRenderer.resetColor();
        GlStateManager.popMatrix();
    }
}