package igentuman.bfr.client.gui;

import java.util.List;
import javax.annotation.Nonnull;

import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge;
import mekanism.client.gui.element.gauge.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

public class GuiFusionReactorHeat extends GuiFusionReactorInfo {

    private static final double MAX_LEVEL = 500_000_000;

    public GuiFusionReactorHeat(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            BFReactorMultiblockData multiblock = tile.getMultiblock();
            return List.of(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.energyContainer)),
                    GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(multiblock.getPassiveGeneration(false, true))));
        }));
        addRenderableWidget(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, MekanismRenderer.FluidTextureType.STILL);
            }

            @Override
            public double getLevel() {
                return tile.getMultiblock().getLastPlasmaTemp();
            }

            @Override
            public double getScaledLevel() {
                return Math.min(1, getLevel() / MAX_LEVEL);
            }

            @Override
            public Component getText() {
                return GeneratorsLang.REACTOR_PLASMA.translate(MekanismUtils.getTemperatureDisplay(getLevel(), TemperatureUnit.KELVIN, true));
            }
        }, GaugeType.STANDARD, this, 7, 50));
        addRenderableWidget(new GuiProgress(() -> {
            BFReactorMultiblockData multiblock = tile.getMultiblock();
            return multiblock.getLastPlasmaTemp() > multiblock.getLastCaseTemp();
        }, ProgressType.SMALL_RIGHT, this, 29, 76));
        addRenderableWidget(new GuiNumberGauge(new INumberInfoHandler() {
            @Override
            public TextureAtlasSprite getIcon() {
                return MekanismRenderer.getBaseFluidTexture(Fluids.LAVA, MekanismRenderer.FluidTextureType.STILL);
            }

            @Override
            public double getLevel() {
                return tile.getMultiblock().getLastCaseTemp();
            }

            @Override
            public double getScaledLevel() {
                return Math.min(1, getLevel() / MAX_LEVEL);
            }

            @Override
            public Component getText() {
                return GeneratorsLang.REACTOR_CASE.translate(MekanismUtils.getTemperatureDisplay(getLevel(), TemperatureUnit.KELVIN, true));
            }
        }, GaugeType.STANDARD, this, 61, 50));
        addRenderableWidget(new GuiProgress(() -> tile.getMultiblock().getCaseTemp() > 0, ProgressType.SMALL_RIGHT, this, 83, 61));
        addRenderableWidget(new GuiProgress(() -> {
            BFReactorMultiblockData multiblock = tile.getMultiblock();
            return multiblock.getCaseTemp() > 0 && !multiblock.liquidCoolantTank.isEmpty() && multiblock.hotCoolantTank.getStored() < multiblock.hotCoolantTank.getCapacity();
        }, ProgressType.SMALL_RIGHT, this, 83, 91));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().liquidCoolantTank, () -> tile.getFluidTanks(null), GaugeType.SMALL, this, 115, 84));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().gasCoolantTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 115, 84));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().hotCoolantTank, () -> tile.getGasTanks(null), GaugeType.SMALL, this, 151, 84));
        addRenderableWidget(new GuiEnergyGauge(tile.getMultiblock().energyContainer, GaugeType.SMALL, this, 115, 46));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.EFFICIENCY));

    }

    @Override
    protected void drawForegroundText(@Nonnull GuiGraphics matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleLabelY);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}