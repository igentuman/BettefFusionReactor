package igentuman.bfr.common.config;

import igentuman.bfr.client.jei.recipe.FusionJEIRecipe;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.config.BaseMekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.config.ModConfig.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BfrConfig extends BaseMekanismConfig {

    private static final String FUSION_CATEGORY = "better_fusion_reactor";
    private static final String IRRADIATOR_CATEGORY = "irradiator";

    private final ForgeConfigSpec configSpec;

    public final CachedIntValue irradiatorBaseProcessTicks;
    public final CachedIntValue irradiatorCoolingRate;
    public final CachedIntValue reactionDifficulty;
    public final CachedBooleanValue reactorMeltdown;
    public final CachedBooleanValue hideMekanismRecipes;
    public final CachedFloatValue reactorExplosionRadius;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fusionCoolants;

    public List<Fluid> allowedCoolantFluids;
    public List<Gas> allowedCoolantGases;
    public List<Gas> allowedCoolantHotGases;

    public HashMap<Gas, Object> coolantMap;
    private static FluidStack resolveFluidIgredient(String name, int amount)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("FluidName", name);
        tag.putInt("Amount", amount);
        return FluidStack.loadFluidStackFromNBT(tag);
    }

    private static GasStack resolveGasIgredient(String name, long amount)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("gasName", name);
        tag.putLong(NBTConstants.AMOUNT, amount);
        return GasStack.readFromNBT(tag);
    }

    public void initFusionCoolants()
    {
        if(coolantMap != null) {
            return;
        }
        coolantMap = new HashMap<>();
        allowedCoolantFluids = new ArrayList<>();
        allowedCoolantGases = new ArrayList<>();
        allowedCoolantHotGases = new ArrayList<>();
        for(String recipe: BetterFusionReactorConfig.bfr.fusionCoolants.get()) {
            String cold = recipe.split(";")[0];
            String hot = recipe.split(";")[1];
            GasStack inputGas = resolveGasIgredient(cold, 1);
            GasStack outputGas = resolveGasIgredient(hot, 1);
            Object coolantCold = null;
            if(inputGas.isEmpty()) {
                //Probably liquid
                FluidStack inputFluid = resolveFluidIgredient(cold,1);
                coolantCold = inputFluid.getFluid();
                if(!inputFluid.isEmpty()) {
                    allowedCoolantFluids.add(inputFluid.getFluid());
                } else {
                    Mekanism.logger.warn("Invalid coolant input: " + cold);
                }
            } else {
                coolantCold = inputGas.getType();
                allowedCoolantGases.add(inputGas.getType());
            }

            if(!outputGas.isEmpty()) {
                if(coolantCold != null) {
                    coolantMap.put(outputGas.getType(), coolantCold);
                }
                allowedCoolantHotGases.add(outputGas.getType());
            } else {
                Mekanism.logger.warn("Invalid coolant output: " + hot);
            }
        }
    }


    BfrConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Better Fusion Reactor. This config is synced between server and client.").push("bfr");

        builder.comment("Irradiator").push(IRRADIATOR_CATEGORY);
        irradiatorBaseProcessTicks = CachedIntValue.wrap(this, builder.comment("Default process time in ticks", "Recipe can override this value")
                .defineInRange("base_process_time", 200, 1, 10000));
        irradiatorCoolingRate = CachedIntValue.wrap(this, builder.comment("How much temperature production by reactor will be suppressed by Irradiator")
                .defineInRange("reactor_cooling", 5, 1, 10));
        builder.pop();

        builder.comment("Better Fusion Reactor").push(FUSION_CATEGORY);
        reactionDifficulty = CachedIntValue.wrap(this, builder.comment("How often Reactivity changes and error level change speed. default 10")
                .defineInRange("reaction_difficulty", 10, 1, 20));
        hideMekanismRecipes = CachedBooleanValue.wrap(this, builder.comment("Hide original Mekanism Fusion Reactor recipes")
                .define("hide_original_mek_recipes", true));
        reactorMeltdown = CachedBooleanValue.wrap(this, builder.comment("Explosion when reactor reaches 100% error level")
                .define("reactor_meltdown", false));
        reactorExplosionRadius = CachedFloatValue.wrap(this, builder.comment("Radius of Explosion (default 4 - TNT size)")
                .define("reactor_explosion_radius", 4.0));
        fusionCoolants = builder.comment("List of fluids that can be used as coolants in the fusion reactor (; separated)")
                .defineList("fusion_coolants", () -> {
                    return List.of(
                            "water;mekanism:steam",
                            "mekanism:sodium;mekanism:superheated_sodium"
                    );
                }, o -> o instanceof String);
        builder.pop();

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "bfr";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.SERVER;
    }
}
