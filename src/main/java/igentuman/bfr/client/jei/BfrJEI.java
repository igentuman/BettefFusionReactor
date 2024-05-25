package igentuman.bfr.client.jei;

import javax.annotation.Nonnull;

import mekanism.client.jei.CatalystRegistryHelper;
import mekanism.client.jei.MekanismJEI;
import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.registries.BfrBlocks;
import mekanism.client.jei.RecipeRegistryHelper;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static igentuman.bfr.client.jei.BfrJEIRecipeType.FUSION;
import static igentuman.bfr.client.jei.BfrJEIRecipeType.IRRADIATOR;
import static igentuman.bfr.common.config.BetterFusionReactorConfig.bfr;


@JeiPlugin
public class BfrJEI implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return BetterFusionReactor.rl("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistration registry) {
        MekanismJEI.registerItemSubtypes(registry, BfrBlocks.BLOCKS.getAllBlocks());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FusionReactorRecipeCategory(guiHelper, FUSION));
        registry.addRecipeCategories(new IrradiatorRecipeCategory(guiHelper, IRRADIATOR));
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, FUSION,
        BfrBlocks.FUSION_REACTOR_CONTROLLER, BfrBlocks.FUSION_REACTOR_PORT);
        CatalystRegistryHelper.register(registry, IRRADIATOR, BfrBlocks.IRRADIATOR);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        RecipeRegistryHelper.register(registry, FUSION, FusionReactorRecipeCategory.getFusionRecipes());
        RecipeRegistryHelper.register(registry, IRRADIATOR, IrradiatorRecipeCategory.getRecipes());
        Collection<ItemStack> collection = Arrays.asList(
                GeneratorsBlocks.LASER_FOCUS_MATRIX,
                GeneratorsBlocks.FUSION_REACTOR_CONTROLLER,
                GeneratorsBlocks.FUSION_REACTOR_FRAME,
                GeneratorsBlocks.FUSION_REACTOR_PORT,
                GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER
        ).stream().map(ItemStack::new).collect(Collectors.toList());
        if(bfr.hideMekanismRecipes.get()) {
            registry.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, collection);
        }
    }
}