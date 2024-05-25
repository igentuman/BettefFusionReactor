package igentuman.bfr.common.tile;

import igentuman.bfr.common.config.BetterFusionReactorConfig;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.recipe.impl.IrradiatingIRecipe;
import igentuman.bfr.common.registries.BfrBlocks;
import igentuman.bfr.common.registries.BfrRecipes;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class TileEntityIrradiator extends TileEntityMachine {
    private final boolean supportsUpgrades = false;
    protected double radiativeFlux = 0;
    public boolean hasRadiationSource = false;

    public double reactorCoolingRate = 0;

    //update source each 20 ticks
    protected int updateSourceTimer = 20;
    public BlockEntity radiationSource;
    public double fluxAggregated = 0;

    public TileEntityIrradiator(BlockPos pos, BlockState state) {
        super(BfrBlocks.IRRADIATOR, pos, state, 200);
        configComponent.addDisabledSides(RelativeSide.BACK);
        if(BetterFusionReactorConfig.bfr.isLoaded()) {
            reactorCoolingRate = (10 - BetterFusionReactorConfig.bfr.irradiatorCoolingRate.get()) / 100000.0+0.9999;
        }
    }

    Field recipeOperatingTicks;

    @Override
    protected void onUpdateServer() {
        updateRadiativeFlux();
        if(radiativeFlux > 0) {
            boolean process = recipeCacheLookupMonitor.updateAndProcess();
            if(process) {
                fluxAggregated += radiativeFlux-1;
                if(((int)fluxAggregated) != 0) {
                    if(recipeOperatingTicks == null) {
                        try {
                            recipeOperatingTicks = currentRecipe.getClass().getSuperclass().getDeclaredField("operatingTicks");
                            recipeOperatingTicks.setAccessible(true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        recipeOperatingTicks.set(currentRecipe, getOperatingTicks() + ((int)fluxAggregated));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    setOperatingTicks(getOperatingTicks() + ((int)fluxAggregated));
                    cooldownRadiativeFluxSource();
                    fluxAggregated = 0;
                }
            }
        }

        if (ejectorComponent != null) {
            ejectorComponent.tickServer();
        }
    }

    private void cooldownRadiativeFluxSource() {
        if(radiationSource instanceof TileEntityFusionReactorPort) {
            BFReactorMultiblockData reactor = ((TileEntityFusionReactorPort)radiationSource).getMultiblock();
            if(reactor != null) {
                if(reactor.isBurning()) {
                    reactor.setPlasmaTemp(reactor.plasmaTemperature*reactorCoolingRate);
                    reactor.setLastPlasmaTemp(reactor.getLastPlasmaTemp()*reactorCoolingRate);
                    return;
                }
            }
        }
        if(radiationSource instanceof TileEntityFissionReactorPort) {
            FissionReactorMultiblockData reactor = ((TileEntityFissionReactorPort)radiationSource).getMultiblock();
            if(reactor != null) {
                if(reactor.isBurning()) {
                    double temperature = reactor.getTotalTemperature()*reactorCoolingRate;
                    reactor.handleHeat(reactor.getTotalTemperature()-temperature);
                }
            }
        }
    }

    @Override
    protected void addGeneralPersistentData(CompoundTag data) {
        super.addGeneralPersistentData(data);
        data.putDouble("radiativeFlux", radiativeFlux);
        data.putBoolean("hasRadiationSource", hasRadiationSource);
    }

    @Override
    protected void loadGeneralPersistentData(CompoundTag data) {
        super.loadGeneralPersistentData(data);
        radiativeFlux = data.getDouble("radiativeFlux");
        hasRadiationSource = data.getBoolean("hasRadiationSource");
    }

    @Override
    public @NotNull IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return BfrRecipes.IRRADIATING;
    }

    private double getRadiationFromFissionReactor()
    {
        TileEntityFissionReactorPort port = (TileEntityFissionReactorPort)radiationSource;
        FissionReactorMultiblockData reactor = port.getMultiblock();
        if(reactor == null) return 0;
        if(!reactor.isBurning()) return  0;
        double burnRateRelation = (reactor.lastBurnRate/reactor.getMaxBurnRate()+1.3)/2;
        //possible burn rate is 0.1-1920
        //rate less 10 will affect irradiation in a negative way
        //we also count burn rate relation
        return Math.min((reactor.lastBurnRate/20)*burnRateRelation, 100);
    }

    private double getRadiationFromFusionReactor()
    {
        TileEntityFusionReactorPort port = (TileEntityFusionReactorPort)radiationSource;
        BFReactorMultiblockData reactor = port.getMultiblock();
        if(reactor == null) return 0;
        if(!reactor.isBurning()) return  0;

        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        //so if burn rate lower than 10 will affect irradiation in a negative way ( at 2 burn rate will be x0.2)
        double lowTemperature = 10 * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() / 0.2 *
                (0.2 + caseAirConductivity) / caseAirConductivity;
        //capping max temperature to 200 of burn rate. so we will have around 20x irradiation max
        //98 burn rate ~x10 irradiation
        double highTemperature = 200 * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() / 0.2 *
                (0.2 + caseAirConductivity) / caseAirConductivity;
        double temperature = Math.min(reactor.plasmaTemperature, highTemperature);
        return temperature/lowTemperature;
    }



    public void updateRadiativeFlux()
    {
        boolean updateFlag = hasRadiationSource;
        updateSourceTimer--;
        if(updateSourceTimer <= 0) {
            updateSourceTimer = 20;
            BlockEntity be = level.getBlockEntity(worldPosition.relative(getDirection().getOpposite(), 1));
            if(be instanceof TileEntityFusionReactorPort || be instanceof TileEntityFissionReactorPort) {
                radiationSource = be;
            } else {
                radiationSource = null;
            }
            double fluxBefore = radiativeFlux;
            if(radiationSource != null) {
                if(radiationSource instanceof TileEntityFissionReactorPort) {
                    setRadiativeFlux((double) Math.round((getRadiationFromFissionReactor()) / 0.01) /100);
                    hasRadiationSource = true;
                } else if(radiationSource instanceof TileEntityFusionReactorPort) {
                    setRadiativeFlux((double) Math.round((getRadiationFromFusionReactor()) / 0.01) /100);
                    hasRadiationSource = true;
                } else {
                    setRadiativeFlux(0);
                }
            } else {
                setRadiativeFlux(0);
                hasRadiationSource = false;
            }
            updateFlag = updateFlag != hasRadiationSource;
            if(fluxBefore != getRadiativeFlux() || updateFlag) {
                markForSave();
                sendUpdatePacket();
            }
        }
    }


    public double getRadiativeFlux()
    {
        return radiativeFlux;
    }

    public void setRadiativeFlux(double value)
    {
        radiativeFlux = value;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        super.handleUpdateTag(tag);
        radiativeFlux = tag.getDouble("radiativeFlux");
        hasRadiationSource = tag.getBoolean("hasRadiationSource");
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();

        updateTag.putDouble("radiativeFlux", radiativeFlux);
        updateTag.putBoolean("hasRadiationSource", hasRadiationSource);
        return updateTag;
    }

    protected CachedRecipe<ItemStackToItemStackRecipe> currentRecipe;
    @NotNull
    @Override
    public CachedRecipe<ItemStackToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackToItemStackRecipe recipe, int cacheIndex) {
        ticksRequired = ((IrradiatingIRecipe)recipe).getTicks();
        currentRecipe =  OneInputCachedRecipe.itemToItem(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
                .setErrorsChanged(this::onErrorsChanged)
                .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
                .setActive(this::setActive)
                .setEnergyRequirements(() -> FloatingLong.create(0), getEnergyContainer())
                .setRequiredTicks(this::getTicksRequired)
                .setOnFinish(this::markForSave)
                .setOperatingTicksChanged(this::setOperatingTicks);
        return currentRecipe;
    }
}