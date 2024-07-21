package igentuman.bfr.common.tile.fusion;

import javax.annotation.Nonnull;

import igentuman.bfr.common.compat.oc2.FusionLogicPortOC2Device;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.NBTUtils;
import igentuman.bfr.common.BfrLang;
import igentuman.bfr.common.base.IReactorLogic;
import igentuman.bfr.common.base.IReactorLogicMode;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.registries.BfrBlocks;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;

public class TileEntityFusionReactorLogicAdapter extends TileEntityFusionReactorBlock implements IReactorLogic<FusionReactorLogic>, IHasMode {

    public FusionReactorLogic logicType = FusionReactorLogic.READY;
    private boolean activeCooled;

    public TileEntityFusionReactorLogicAdapter(BlockPos pos, BlockState state) {
        super(BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER, pos, state);
        if(ModList.get().isLoaded("oc2r")) {
            addCapabilityResolver(BasicCapabilityResolver.constant(FusionLogicPortOC2Device.DEVICE_CAPABILITY, FusionLogicPortOC2Device.createDevice(this)));
        }
    }

    @Override
    protected boolean onUpdateServer(BFReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        int redstone = checkMode();
        if (redstone != prevRedstoneLevel) {
            Level world = getLevel();
            if (world != null) {
                Direction side = multiblock.getOutsideSide(worldPosition);
                if (side == null) {
                    //Not formed, just update all sides
                    world.updateNeighborsAt(getBlockPos(), getBlockType());
                } else if (!ForgeEventFactory.onNeighborNotify(world, worldPosition, getBlockState(), EnumSet.of(side), false).isCanceled()) {
                    world.neighborChanged(worldPosition.relative(side), getBlockType(), worldPosition);
                }
            }
            prevRedstoneLevel = redstone;
        }

        return needsPacket;
    }

    public void onPowerChange()
    {
        if(isPowered() && !wasPowered()) {
            BFReactorMultiblockData multiblock = getMultiblock();
            if(multiblock == null || !getMultiblock().isFormed()) {
                return;
            }
            int power = getTileWorld().getBestNeighborSignal(getBlockPos());
            switch (logicType) {
                case INJECTION_DOWN:
                    multiblock.setInjectionRate(multiblock.getInjectionRate()-2);
                    break;
                case INJECTION_UP:
                    multiblock.setInjectionRate(multiblock.getInjectionRate()+2);
                    break;
                case REACTIVITY_UP:
                    multiblock.adjustReactivity(power);
                    break;
                case REACTIVITY_DOWN:
                    multiblock.adjustReactivity(-power);
                    break;
                default:
                    return;
            }
            markForSave();
        }
    }

    protected int prevRedstoneLevel;

    public int getRedstoneLevel(Direction side)
    {
       return !isRemote() && getMultiblock().isPositionOutsideBounds(worldPosition.relative(side)) ? checkMode() : 0;
    }

    public int checkMode() {
        if (isRemote()) {
            return prevRedstoneLevel;
        }
        BFReactorMultiblockData multiblock = getMultiblock();
        if (multiblock == null || !getMultiblock().isFormed()) {
            return 0;
        }
        switch (logicType) {
            case READY:
                return multiblock.getLastPlasmaTemp() >= multiblock.getIgnitionTemperature(activeCooled)  ? 15 : 0;
            case CAPACITY:
                return multiblock.getLastPlasmaTemp() >= multiblock.getMaxPlasmaTemperature(activeCooled) ? 15 : 0;
            case ERROR_LEVEL:
                return (int)(multiblock.getErrorLevel() / (100 / 15));
            case EFFICIENCY:
                return (int)(multiblock.getEfficiency() / (100 / 15));
            case DEPLETED:
                return (multiblock.deuteriumTank.getStored() < multiblock.getInjectionRate() / 2) ||
                        (multiblock.tritiumTank.getStored() < multiblock.getInjectionRate() / 2) ? 15 : 0;
            default:
                return 0;
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbtTags) {
        super.load(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.LOGIC_TYPE, FusionReactorLogic::byIndexStatic, logicType -> this.logicType = logicType);
        activeCooled = nbtTags.getBoolean(NBTConstants.ACTIVE_COOLED);
    }

    @Nonnull
    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putInt(NBTConstants.LOGIC_TYPE, logicType.getId());
        nbtTags.putBoolean(NBTConstants.ACTIVE_COOLED, activeCooled);
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    @Override
    public void nextMode() {
        activeCooled = !activeCooled;
        markForSave();
    }

    @Override
    public void previousMode() {

    }

    @ComputerMethod(nameOverride = "isActiveCooledLogic")
    public boolean isActiveCooled() {
        return activeCooled;
    }

    @Override
    @ComputerMethod(nameOverride = "getLogicMode")
    public FusionReactorLogic getMode() {
        return logicType;
    }

    @Override
    public FusionReactorLogic[] getModes() {
        return FusionReactorLogic.values();
    }

    protected ArrayList<FusionReactorLogic> inLogicModes;

    public FusionReactorLogic[] getInputModes() {
        if(inLogicModes == null) {
            inLogicModes = new ArrayList<>();
            for(int i = 0; i < FusionReactorLogic.values().length; i++) {
                if(Objects.equals(FusionReactorLogic.byIndexStatic(i).direction, "in")) {
                    inLogicModes.add(FusionReactorLogic.values()[i]);
                }
            }
        }
        return inLogicModes.toArray(new FusionReactorLogic[0]);
    }

    protected ArrayList<FusionReactorLogic> outLogicModes;

    public FusionReactorLogic[] getOutputModes() {
        if(outLogicModes == null) {
            outLogicModes = new ArrayList<>();
            for(int i = 0; i < FusionReactorLogic.values().length; i++) {
                if(Objects.equals(FusionReactorLogic.byIndexStatic(i).direction, "out")) {
                    outLogicModes.add(FusionReactorLogic.values()[i]);
                }
            }
        }
        return outLogicModes.toArray(new FusionReactorLogic[0]);
    }

    @ComputerMethod(nameOverride = "setLogicMode")
    public void setLogicTypeFromPacket(FusionReactorLogic logicType) {
        if (this.logicType != logicType) {
            this.logicType = logicType;
            markForSave();
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(FusionReactorLogic::byIndexStatic, FusionReactorLogic.READY, this::getMode, value -> logicType = value));
        container.track(SyncableBoolean.create(this::isActiveCooled, value -> activeCooled = value));
        container.track(SyncableInt.create(() -> prevRedstoneLevel, value -> prevRedstoneLevel = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    public void setActiveCooledLogic(boolean active) {
        if (activeCooled != active) {
            nextMode();
        }
    }
    //End methods IComputerTile

    public enum FusionReactorLogic implements IReactorLogicMode<FusionReactorLogic>, IHasTranslationKey {
        READY(0, GeneratorsLang.REACTOR_LOGIC_READY, GeneratorsLang.DESCRIPTION_REACTOR_READY, new ItemStack(Items.REDSTONE), "out"),
        CAPACITY(1, GeneratorsLang.REACTOR_LOGIC_CAPACITY, GeneratorsLang.DESCRIPTION_REACTOR_CAPACITY, new ItemStack(Items.REDSTONE), "out"),
        DEPLETED(2, GeneratorsLang.REACTOR_LOGIC_DEPLETED, GeneratorsLang.DESCRIPTION_REACTOR_DEPLETED, new ItemStack(Items.REDSTONE), "out"),
        EFFICIENCY(3, BfrLang.REACTOR_LOGIC_EFFICIENCY, BfrLang.DESCRIPTION_REACTOR_EFFICIENCY, new ItemStack(Items.REDSTONE), "out"),
        ERROR_LEVEL(4, BfrLang.REACTOR_LOGIC_ERROR_LEVEL, BfrLang.DESCRIPTION_REACTOR_ERROR_LEVEL, new ItemStack(Items.REDSTONE), "out"),
        INJECTION_UP(5, BfrLang.REACTOR_LOGIC_INJECTION_UP, BfrLang.DESCRIPTION_REACTOR_INJECTION_UP, new ItemStack(Items.REDSTONE), "in"),
        INJECTION_DOWN(6, BfrLang.REACTOR_LOGIC_INJECTION_DOWN, BfrLang.DESCRIPTION_REACTOR_INJECTION_DOWN, new ItemStack(Items.REDSTONE), "in"),
        REACTIVITY_UP(7, BfrLang.REACTOR_LOGIC_REACTIVITY_UP, BfrLang.DESCRIPTION_REACTOR_REACTIVITY_UP, new ItemStack(Items.REDSTONE), "in"),
        REACTIVITY_DOWN(8, BfrLang.REACTOR_LOGIC_REACTIVITY_DOWN, BfrLang.DESCRIPTION_REACTOR_REACTIVITY_DOWN, new ItemStack(Items.REDSTONE), "in");

        private static final FusionReactorLogic[] MODES = values();

        private final int id;
        private final ILangEntry name;
        private final ILangEntry description;
        private final ItemStack renderStack;
        private final String direction;

        FusionReactorLogic(int id, ILangEntry name, ILangEntry description, ItemStack stack, String dir) {
            this.id = id;
            this.name = name;
            this.description = description;
            renderStack = stack;
            direction = dir;
        }

        @Override
        public ItemStack getRenderStack() {
            return renderStack;
        }

        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }

        @Override
        public Component getDescription() {
            return description.translate();
        }

        @Override
        public EnumColor getColor() {
            return EnumColor.RED;
        }

        public int getId() {
            return id;
        }

        public static FusionReactorLogic byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}