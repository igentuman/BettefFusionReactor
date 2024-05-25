package igentuman.bfr.client.gui;


import java.util.Arrays;
import javax.annotation.Nonnull;

import igentuman.bfr.common.config.BetterFusionReactorConfig;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import igentuman.bfr.common.BfrLang;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiFusionReactorController extends GuiMekanismTile<TileEntityFusionReactorController, MekanismTileContainer<TileEntityFusionReactorController>> {

    public GuiFusionReactorController(MekanismTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        if (tile.getMultiblock().isFormed()) {
            addRenderableWidget(new GuiEnergyTab(this, () -> {
                BFReactorMultiblockData multiblock = tile.getMultiblock();
                return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.energyContainer)),
                      GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, true))));
            }));
            addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
            addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
            addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
            addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.EFFICIENCY));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull GuiGraphics matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleLabelY);
        drawString(matrix, MekanismLang.MULTIBLOCK_FORMED.translate(), 8, 16, titleTextColor());
        if(tile.getMultiblock().isBurning()) {
            if(tile.getMultiblock().getEfficiency() >= 80) {
                drawString(matrix, BfrLang.EFFICIENCY_GOOD.translate(), 8, 70, 0x097969);
            } else {
                drawString(matrix, BfrLang.EFFICIENCY_BAD.translate(), 8, 60, 0xC70039);
                if(BetterFusionReactorConfig.bfr.reactorMeltdown.get() && BetterFusionReactorConfig.bfr.reactorExplosionRadius.get() > 0) {
                    drawString(matrix, BfrLang.MIGHT_EXPLODE.translate(), 8, 70, 0xC70039);
                } else {
                    drawString(matrix, BfrLang.MIGHT_TURNOFF.translate(), 8, 70, 0xC70039);
                }
            }
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}