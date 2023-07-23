package igentuman.bfr.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GuiIrradiationSlot extends GuiSlot {

    private static final int INVALID_SLOT_COLOR = MekanismRenderer.getColorARGB(EnumColor.DARK_RED, 0.8F);
    public static final int DEFAULT_HOVER_COLOR = 0x80FFFFFF;
    private final SlotType slotType;
    private Supplier<ItemStack> validityCheck;
    private Supplier<ItemStack> storedStackSupplier;
    private Supplier<SlotOverlay> overlaySupplier;
    @Nullable
    private BooleanSupplier warningSupplier;
    @Nullable
    private IntSupplier overlayColorSupplier;
    @Nullable
    private SlotOverlay overlay;
    @Nullable
    private IHoverable onHover;
    @Nullable
    private IClickable onClick;
    private boolean renderHover;
    private boolean renderAboveSlots;

    @Nullable
    private IGhostIngredientConsumer ghostHandler;

    public GuiIrradiationSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        this.slotType = type;
        active = false;
    }

    public GuiIrradiationSlot validity(Supplier<ItemStack> validityCheck) {
        //TODO - 1.18: Evaluate if any of these validity things should be moved to the warning system
        this.validityCheck = validityCheck;
        return this;
    }

    @Override
    public GuiIrradiationSlot warning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    /**
     * @apiNote For use when there is no validity check and this is a "fake" slot in that the container screen doesn't render the item by default.
     */
    public GuiIrradiationSlot stored(Supplier<ItemStack> storedStackSupplier) {
        this.storedStackSupplier = storedStackSupplier;
        return this;
    }

    public GuiIrradiationSlot hover(IHoverable onHover) {
        this.onHover = onHover;
        return this;
    }

    public GuiIrradiationSlot click(IClickable onClick) {
        //Use default click sound
        return click(onClick, SoundEvents.UI_BUTTON_CLICK);
    }

    public GuiIrradiationSlot click(IClickable onClick, @Nullable SoundEvent clickSound) {
        this.clickSound = clickSound;
        this.onClick = onClick;
        return this;
    }

    public GuiIrradiationSlot with(SlotOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public GuiIrradiationSlot overlayColor(IntSupplier colorSupplier) {
        overlayColorSupplier = colorSupplier;
        return this;
    }

    public GuiIrradiationSlot with(Supplier<SlotOverlay> overlaySupplier) {
        this.overlaySupplier = overlaySupplier;
        return this;
    }

    public GuiIrradiationSlot setRenderHover(boolean renderHover) {
        this.renderHover = renderHover;
        return this;
    }

    public GuiIrradiationSlot setGhostHandler(@Nullable IGhostIngredientConsumer ghostHandler) {
        this.ghostHandler = ghostHandler;
        return this;
    }

    public GuiIrradiationSlot setRenderAboveSlots() {
        this.renderAboveSlots = true;
        return this;
    }

    @Override
    public void renderButton(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (!renderAboveSlots) {
            draw(matrix);
        }
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (renderAboveSlots) {
            draw(matrix);
        }
    }

    private void draw(@NotNull PoseStack matrix) {
        if (warningSupplier != null && warningSupplier.getAsBoolean()) {
            RenderSystem.setShaderTexture(0, slotType.getWarningTexture());
        } else {
            RenderSystem.setShaderTexture(0, getResource());
        }
        blit(matrix, x, y, 0, 0, width, height, width, height);
        if (overlaySupplier != null) {
            overlay = overlaySupplier.get();
        }
        if (overlay != null) {
            RenderSystem.setShaderTexture(0, overlay.getTexture());
            blit(matrix, x, y, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
        }
        drawContents(matrix);
    }

    protected void drawContents(@NotNull PoseStack matrix) {
        if (validityCheck != null) {
            ItemStack invalid = validityCheck.get();
            if (!invalid.isEmpty()) {
                int xPos = x + 1;
                int yPos = y + 1;
                fill(matrix, xPos, yPos, xPos + 16, yPos + 16, INVALID_SLOT_COLOR);
                MekanismRenderer.resetColor();
                gui().renderItem(matrix, invalid, xPos, yPos);
            }
        } else if (storedStackSupplier != null) {
            ItemStack stored = storedStackSupplier.get();
            if (!stored.isEmpty()) {
                gui().renderItem(matrix, stored, x + 1, y + 1);
            }
        }
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        if (renderHover && isHoveredOrFocused()) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            fill(matrix, xPos, yPos, xPos + 16, yPos + 16, DEFAULT_HOVER_COLOR);
            MekanismRenderer.resetColor();
        }
        if (overlayColorSupplier != null) {
            matrix.pushPose();
            matrix.translate(0, 0, 10);
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            fill(matrix, xPos, yPos, xPos + 16, yPos + 16, overlayColorSupplier.getAsInt());
            matrix.popPose();
            MekanismRenderer.resetColor();
        }
        if (isHoveredOrFocused()) {
            //TODO: Should it pass it the proper mouseX and mouseY. Probably, though buttons may have to be redone slightly then
            renderToolTip(matrix, mouseX - getGuiLeft(), mouseY - getGuiTop());
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, tooltip);
        if (onHover != null) {
            onHover.onHover(this, matrix, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (onClick != null && isValidClickButton(button)) {
            if (mouseX >= x + borderSize() && mouseY >= y + borderSize() && mouseX < x + width - borderSize() && mouseY < y + height - borderSize()) {
                if (onClick.onClick(this, (int) mouseX, (int) mouseY)) {
                    playDownSound(minecraft.getSoundManager());
                    return true;
                }
                //If clicking the slot fails check super as maybe it has children that can handle clicks
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    @Override
    public IGhostIngredientConsumer getGhostHandler() {
        return ghostHandler;
    }

    @Override
    public int borderSize() {
        return 1;
    }

    public List<Component> tooltip = new ArrayList<>();
    public void setTooltip(MutableComponent translatable) {
        tooltip.clear();
        tooltip.add(translatable);
    }

    public void addTooltip(MutableComponent translatable) {
        tooltip.add(translatable);
    }
}