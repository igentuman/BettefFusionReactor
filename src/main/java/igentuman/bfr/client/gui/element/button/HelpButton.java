package igentuman.bfr.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.bfr.common.BfrLang;
import igentuman.bfr.datagen.lang.BfrLangProvider;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class HelpButton extends MekanismButton {
    private final int index;


    public HelpButton(IGuiWrapper gui, int x, int y, int index) {
        super(gui, x, y, 14, 14, TextComponentUtil.getString("?"),  () -> {}, null);
        this.index = index;
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, BfrLang.REACTOR_HELP1.translate(),BfrLang.REACTOR_HELP2.translate(), BfrLang.REACTOR_HELP3.translate());
    }
}