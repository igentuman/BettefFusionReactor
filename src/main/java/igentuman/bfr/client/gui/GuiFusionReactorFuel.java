package igentuman.bfr.client.gui;

import java.util.Arrays;
import javax.annotation.Nonnull;

import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab;
import igentuman.bfr.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.network.to_server.PacketBfrGuiInteract;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiFusionReactorFuel extends GuiFusionReactorInfo {

    private GuiTextField injectionRateField;

    public GuiFusionReactorFuel(EmptyTileContainer<TileEntityFusionReactorController> container, Inventory inv, Component title) {
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
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().deuteriumTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.SMALL, this, 25, 64));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().fuelTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 79, 50));
        addRenderableWidget(new GuiGasGauge(() -> tile.getMultiblock().tritiumTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.SMALL, this, 133, 64));
        addRenderableWidget(new GuiProgress(() -> tile.getMultiblock().isBurning(), ProgressType.SMALL_RIGHT, this, 47, 76));
        addRenderableWidget(new GuiProgress(() -> tile.getMultiblock().isBurning(), ProgressType.SMALL_LEFT, this, 101, 76));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.STAT));
        addRenderableWidget(new GuiFusionReactorTab(this, tile, FusionReactorTab.EFFICIENCY));

        injectionRateField = addRenderableWidget(new GuiTextField(this, 98, 115, 26, 11));
        injectionRateField.setFocused(true);
        injectionRateField.setInputValidator(InputValidator.DIGIT);
        injectionRateField.setEnterHandler(this::setInjection);
        injectionRateField.setMaxLength(3);
    }

    @Override
    protected void drawForegroundText(@Nonnull GuiGraphics matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, GeneratorsLang.FUSION_REACTOR.translate(), titleLabelY);
        drawCenteredText(matrix, GeneratorsLang.REACTOR_INJECTION_RATE.translate(tile.getMultiblock().getInjectionRate()), 0, imageWidth, 35, titleTextColor());
        drawString(matrix, GeneratorsLang.REACTOR_EDIT_RATE.translate(), 50, 117, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private void setInjection() {
        if (!injectionRateField.getText().isEmpty()) {
            BetterFusionReactor.packetHandler().sendToServer(new PacketBfrGuiInteract(PacketBfrGuiInteract.BfrGuiInteraction.INJECTION_RATE, tile, Integer.parseInt(injectionRateField.getText())));
            injectionRateField.setText("");
        }
    }
}