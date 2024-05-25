package igentuman.bfr.common.registries;

import igentuman.bfr.common.tile.TileEntityIrradiator;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorBlock;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorPort;
import igentuman.bfr.common.tile.fusion.TileEntityLaserFocusMatrix;

public class BfrTileEntityTypes {

    private BfrTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(BetterFusionReactor.MODID);
    //Misc
    public static final TileEntityTypeRegistryObject<TileEntityIrradiator> IRRADIATOR = TILE_ENTITY_TYPES.register(BfrBlocks.IRRADIATOR, TileEntityIrradiator::new);

    //Fusion Reactor
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorController> FUSION_REACTOR_CONTROLLER = TILE_ENTITY_TYPES.register(BfrBlocks.FUSION_REACTOR_CONTROLLER, TileEntityFusionReactorController::new);

    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorBlock> FUSION_REACTOR_FRAME = TILE_ENTITY_TYPES.register(BfrBlocks.FUSION_REACTOR_FRAME, TileEntityFusionReactorBlock::new);
    public static final TileEntityTypeRegistryObject<TileEntityLaserFocusMatrix> LASER_FOCUS_MATRIX = TILE_ENTITY_TYPES.register(BfrBlocks.LASER_FOCUS_MATRIX, TileEntityLaserFocusMatrix::new);
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = TILE_ENTITY_TYPES.register(BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER, TileEntityFusionReactorLogicAdapter::new);
    public static final TileEntityTypeRegistryObject<TileEntityFusionReactorPort> FUSION_REACTOR_PORT = TILE_ENTITY_TYPES.register(BfrBlocks.FUSION_REACTOR_PORT, TileEntityFusionReactorPort::new);
}