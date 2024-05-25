package igentuman.bfr.common.tile.fusion;

import igentuman.bfr.common.config.BetterFusionReactorConfig;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tile.base.SubstanceType;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.registries.BfrBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class TileEntityFusionReactorController extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorController(BlockPos pos, BlockState state) {
        super(BfrBlocks.FUSION_REACTOR_CONTROLLER, pos, state);
        //Never allow the gas handler, fluid handler, or energy cap to be enabled here even though internally we can handle both of them
        addDisabledCapabilities(Capabilities.GAS_HANDLER, ForgeCapabilities.FLUID_HANDLER, Capabilities.HEAT_HANDLER);
        addDisabledCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities());
        addSemiDisabledCapability(ForgeCapabilities.ITEM_HANDLER, () -> !getMultiblock().isFormed());
        delaySupplier = () -> 0;
    }

    @Override
    protected boolean onUpdateServer(BFReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        setActive(multiblock.isFormed());
        if(multiblock.explodeFlag) {
            multiblock.explodeFlag = false;
            getTileWorld().explode(null, getTilePos().getX(), (double)getTilePos().getY()+1, getTilePos().getZ(), BetterFusionReactorConfig.bfr.reactorExplosionRadius.get(), true, Level.ExplosionInteraction.TNT);
            return false;
        }
        return needsPacket;
    }

    @Override
    protected boolean canPlaySound() {
        BFReactorMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && multiblock.isBurning();
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }

    @Override
    public boolean handles(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.HEAT) {
            return false;
        }
        return super.handles(type);
    }
}