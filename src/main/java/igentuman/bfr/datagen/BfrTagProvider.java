package igentuman.bfr.datagen;

import javax.annotation.Nullable;

import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.datagen.tag.BaseTagProvider;
import igentuman.bfr.common.registries.BfrBlocks;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BfrTagProvider extends BaseTagProvider {

    public BfrTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BetterFusionReactor.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags(HolderLookup.Provider registries) {
        addEndermanBlacklist();
        addHarvestRequirements();
    }


    private void addEndermanBlacklist() {
        addToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST,
              BfrBlocks.FUSION_REACTOR_CONTROLLER,
              BfrBlocks.FUSION_REACTOR_PORT,
              BfrBlocks.FUSION_REACTOR_FRAME,
              BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER,
              BfrBlocks.LASER_FOCUS_MATRIX,
              BfrBlocks.IRRADIATOR
        );
        for(BlockRegistryObject<Block, BlockItem> ore : BfrBlocks.ORE_BLOCKS.values()) {
            addToTag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST, ore);
        }
    }

    private void addHarvestRequirements() {
        addToHarvestTag(BlockTags.MINEABLE_WITH_PICKAXE,
                BfrBlocks.LASER_FOCUS_MATRIX,
                BfrBlocks.FUSION_REACTOR_CONTROLLER,
                BfrBlocks.FUSION_REACTOR_FRAME,
                BfrBlocks.FUSION_REACTOR_PORT,
                BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER,
                BfrBlocks.IRRADIATOR
        );
        for(BlockRegistryObject<Block, BlockItem> ore : BfrBlocks.ORE_BLOCKS.values()) {
            addToTag(BlockTags.MINEABLE_WITH_PICKAXE, ore);
        }
    }
}