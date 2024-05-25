package igentuman.bfr.common.registries;

import igentuman.bfr.common.tile.TileEntityIrradiator;
import igentuman.bfr.common.tile.fusion.*;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.common.content.blocktype.Machine;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsSounds;


public class BfrBlockTypes {

    private BfrBlockTypes() {
    }

    public static final Machine<TileEntityIrradiator> IRRADIATOR = Machine.MachineBuilder
            .createMachine(() -> BfrTileEntityTypes.IRRADIATOR, MekanismLang.DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR)
            .withGui(() -> BfrContainerTypes.IRRADIATOR)
            .with(new AttributeEnergy(() -> FloatingLong.create(0), () -> FloatingLong.create(0)))
            .withComputerSupport("irradiator")
            .build();

    // Fusion Reactor Controller
    public static final BlockTypeTile<TileEntityFusionReactorController> FUSION_REACTOR_CONTROLLER = BlockTileBuilder
          .createBlock(() -> BfrTileEntityTypes.FUSION_REACTOR_CONTROLLER, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_CONTROLLER)
          .withGui(() -> BfrContainerTypes.FUSION_REACTOR_CONTROLLER)
          .withSound(GeneratorsSounds.FUSION_REACTOR)
            .externalMultiblock()
          .with(Attributes.ACTIVE, Attributes.INVENTORY, AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fusion Reactor Port
    public static final BlockTypeTile<TileEntityFusionReactorPort> FUSION_REACTOR_PORT = BlockTileBuilder
          .createBlock(() -> BfrTileEntityTypes.FUSION_REACTOR_PORT, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_PORT)
          .with(Attributes.ACTIVE, AttributeMobSpawn.WHEN_NOT_FORMED)
            .externalMultiblock()
          .withComputerSupport("fusionReactorPort")
          .build();
    // Fusion Reactor Frame
    public static final BlockTypeTile<TileEntityFusionReactorBlock> FUSION_REACTOR_FRAME = BlockTileBuilder
          .createBlock(() -> BfrTileEntityTypes.FUSION_REACTOR_FRAME, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_FRAME)
          .withEnergyConfig(null, null)
            .externalMultiblock()
          .with(AttributeMobSpawn.WHEN_NOT_FORMED)
          .build();
    // Fusion Reactor Logic Adapter
    public static final BlockTypeTile<TileEntityFusionReactorLogicAdapter> FUSION_REACTOR_LOGIC_ADAPTER = BlockTileBuilder
          .createBlock(() -> BfrTileEntityTypes.FUSION_REACTOR_LOGIC_ADAPTER, GeneratorsLang.DESCRIPTION_FUSION_REACTOR_LOGIC_ADAPTER)
          .withGui(() -> BfrContainerTypes.FUSION_REACTOR_LOGIC_ADAPTER)
          .with(new AttributeRedstoneEmitter<>(TileEntityFusionReactorLogicAdapter::getRedstoneLevel))
          .with(Attributes.REDSTONE)
          .with(AttributeMobSpawn.WHEN_NOT_FORMED)
          .withComputerSupport("fusionReactorLogicAdapter")
          .externalMultiblock()
          .build();
    // Laser Focus Matrix
    public static final BlockTypeTile<TileEntityLaserFocusMatrix> LASER_FOCUS_MATRIX = BlockTileBuilder
            .createBlock(() -> BfrTileEntityTypes.LASER_FOCUS_MATRIX, GeneratorsLang.DESCRIPTION_LASER_FOCUS_MATRIX)
            .with(AttributeMultiblock.EXTERNAL, AttributeMobSpawn.NEVER)
            .build();
}
