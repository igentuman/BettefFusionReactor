package igentuman.bfr.common.tile.fusion;

import javax.annotation.Nonnull;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.registries.BfrBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaserFocusMatrix extends TileEntityFusionReactorBlock implements ILaserReceptor {

    public TileEntityLaserFocusMatrix(BlockPos pos, BlockState state) {
        super(BfrBlocks.LASER_FOCUS_MATRIX, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.LASER_RECEPTOR, this));
    }

    @Override
    public void receiveLaserEnergy(@Nonnull FloatingLong energy) {
        BFReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            multiblock.addTemperatureFromEnergyInput(energy);
            multiblock.processLaserShoot(energy);
        }
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (!isRemote() && player.isCreative()) {
            BFReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                multiblock.setPlasmaTemp(1_000_000_000);
                return InteractionResult.sidedSuccess(isRemote());
            }
        }
        return super.onRightClick(player);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}