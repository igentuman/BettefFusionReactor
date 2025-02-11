package igentuman.bfr.common.content.fusion;

import java.util.EnumSet;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import mekanism.common.lib.math.voxel.VoxelCuboid.WallRelative;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.Structure.Axis;
import mekanism.common.lib.multiblock.StructureHelper;
import igentuman.bfr.common.registries.BfrBlockTypes;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BFReactorValidator extends CuboidStructureValidator<BFReactorMultiblockData> {

    private static final VoxelCuboid BOUNDS = new VoxelCuboid(5, 5, 5);
    private static final byte[][] ALLOWED_GRID = new byte[][]{
          {0, 0, 1, 0, 0},
          {0, 1, 2, 1, 0},
          {1, 2, 2, 2, 1},
          {0, 1, 2, 1, 0},
          {0, 0, 1, 0, 0}
    };

    @Override
    protected StructureRequirement getStructureRequirement(BlockPos pos) {
        WallRelative relative = cuboid.getWallRelative(pos);
        if (relative.isWall()) {
            Axis axis = Axis.get(cuboid.getSide(pos));
            Axis h = axis.horizontal(), v = axis.vertical();
            //Note: This ends up becoming immutable by doing this but that is fine and doesn't really matter
            pos = pos.subtract(cuboid.getMinPos());
            return StructureRequirement.REQUIREMENTS[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
        }
        return super.getStructureRequirement(pos);
    }

    @Override
    protected FormationResult validateFrame(FormationProtocol<BFReactorMultiblockData> ctx, BlockPos pos, BlockState state, CasingType type, boolean needsFrame) {
        boolean isControllerPos = pos.getY() == cuboid.getMaxPos().getY() && pos.getX() == cuboid.getMinPos().getX() + 2 && pos.getZ() == cuboid.getMinPos().getZ() + 2;
        boolean controller = structure.getTile(pos) instanceof TileEntityFusionReactorController;
        if (isControllerPos && !controller) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER);
        } else if (!isControllerPos && controller) {
            return FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT);
        }
        return super.validateFrame(ctx, pos, state, type, needsFrame);
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, BfrBlockTypes.FUSION_REACTOR_FRAME, GeneratorsBlockTypes.FUSION_REACTOR_FRAME)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, BfrBlockTypes.FUSION_REACTOR_PORT)) {
            return CasingType.VALVE;
        } else if (BlockType.is(block, BfrBlockTypes.FUSION_REACTOR_CONTROLLER,
              BfrBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, BfrBlockTypes.LASER_FOCUS_MATRIX, GeneratorsBlockTypes.LASER_FOCUS_MATRIX)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    public boolean precheck() {
        // 72 = (12 missing blocks possible on each face) * (6 sides)
        cuboid = StructureHelper.fetchCuboid(structure, BOUNDS, BOUNDS, EnumSet.allOf(CuboidSide.class), 72);
        return cuboid != null;
    }
}
