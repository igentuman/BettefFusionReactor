package igentuman.bfr.client.gui;

import java.io.IOException;

import igentuman.bfr.common.tile.reactor.TileEntityReactorController;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiReactorInfo extends GuiMekanismTile<TileEntityReactorController> {

    private GuiButton backButton;

    public GuiReactorInfo(TileEntityReactorController tile, Container container) {
        super(tile, container);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(backButton = new GuiButtonDisableableImage(0, guiLeft + 6, guiTop + 6, 14, 14, 176, 14, -14, getGuiLocation()));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == backButton.id) {
            Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 1, 10));
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png");
    }
}