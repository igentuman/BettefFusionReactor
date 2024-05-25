package igentuman.bfr.client.gui;

import java.util.Arrays;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiFusionReactorStats extends GuiFusionReactorInfo {

    public GuiFusionReactorStats(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            BFReactorMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.energyContainer)),
                  GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, true))));
        }));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.EFFICIENCY));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleLabelY);
        BFReactorMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed()) {
            drawString(matrix, GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN), 6, 26, titleTextColor());
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(false)), 16, 36, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 46, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 56, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 66, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, false))),
                  16, 76, titleTextColor(), 156);

            drawString(matrix, GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE), 6, 92, titleTextColor());
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MIN_INJECTION.translate(multiblock.getMinInjectionRate(true)), 16, 102, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(multiblock.getIgnitionTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 112, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxPlasmaTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 122, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(multiblock.getMaxCasingTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 132, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(true, false))),
                  16, 142, titleTextColor(), 156);
            drawTextScaledBound(matrix, GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(TextUtils.format(multiblock.getSteamPerTick(false))),
                  16, 152, titleTextColor(), 156);
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}