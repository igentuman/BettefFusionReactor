package igentuman.bfr.common.tile.fusion;


import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.registries.BfrBlocks;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFusionReactorBlock extends TileEntityMultiblock<BFReactorMultiblockData> {

    public TileEntityFusionReactorBlock(BlockPos pos, BlockState state) {
        this(BfrBlocks.FUSION_REACTOR_FRAME, pos, state);
    }

    public TileEntityFusionReactorBlock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public BFReactorMultiblockData createMultiblock() {
        return new BFReactorMultiblockData(this);
    }

    @Override
    public MultiblockManager<BFReactorMultiblockData> getManager() {
        return BetterFusionReactor.fusionReactorManager;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    public void setInjectionRateFromPacket(int rate) {
        BFReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            multiblock.setInjectionRate(Math.min(BFReactorMultiblockData.MAX_INJECTION, Math.max(0, rate - (rate % 2))));
            markForSave();
        }
    }

    public void adjustReactivityFromPacket(float val) {
        BFReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed() && multiblock.isBurning()) {
            multiblock.adjustReactivity(val);
            markForSave();
        }
    }

    public void addFuelTabContainerTrackers(MekanismContainer container) {
        SyncMapper.INSTANCE.setup(container, BFReactorMultiblockData.class, this::getMultiblock, "fuel");
    }

    public void addHeatTabContainerTrackers(MekanismContainer container) {
        SyncMapper.INSTANCE.setup(container, BFReactorMultiblockData.class, this::getMultiblock, "heat");
    }
}