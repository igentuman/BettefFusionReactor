package igentuman.bfr.common.content.fusion;

import igentuman.bfr.common.config.BetterFusionReactorConfig;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.LongSupplier;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import igentuman.bfr.common.slot.ReactorInventorySlot;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorBlock;
import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorPort;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidType;

import static mekanism.generators.common.content.fusion.FusionReactorMultiblockData.FUEL_TAB;

public class BFReactorMultiblockData extends MultiblockData {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(1_000_000_000);
    private static final int MAX_COOLANT = 1_000 * FluidType.BUCKET_VOLUME;
    private static final long MAX_STEAM = MAX_COOLANT * 100L;
    private static final long MAX_FUEL = FluidType.BUCKET_VOLUME;

    public static final int MAX_INJECTION = 98;//this is the effective cap in the GUI, as text field is limited to 2 chars
    //Reaction characteristics
    private static final double burnTemperature = 100_000_000;
    private static final double burnRatio = 1;
    //Thermal characteristics
    private static final double plasmaHeatCapacity = 100;
    private static final double caseHeatCapacity = 1;
    private static final double inverseInsulation = 100_000;
    //Heat transfer metrics
    private static final double plasmaCaseConductivity = 0.2;

    private final Set<ITileHeatHandler> heatHandlers = new ObjectOpenHashSet<>();
    @ContainerSync(tags = "heat")
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getCoolant", "getCoolantCapacity", "getCoolantNeeded", "getCoolantFilledPercentage"}, docPlaceholder = "coolant tank")
    public IGasTank gasCoolantTank;

    @ContainerSync
    private boolean burning = false;

    @ContainerSync
    public IEnergyContainer energyContainer;
    public IHeatCapacitor heatCapacitor;

    @ContainerSync(tags = "heat")
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getLiquidCoolant", "getLiquidCoolantCapacity", "getLiquidCoolantNeeded", "getLiquidCoolantFilledPercentage"}, docPlaceholder = "cold coolant tank")
    public IExtendedFluidTank liquidCoolantTank;
    @ContainerSync(tags = "heat")
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSteam", "getSteamCapacity", "getSteamNeeded", "getSteamFilledPercentage"}, docPlaceholder = "hot coolant tank")
    public IGasTank hotCoolantTank;

    private double biomeAmbientTemp;
    @ContainerSync(tags = "heat")
    @SyntheticComputerMethod(getter = "getPlasmaTemperature")
    private double lastPlasmaTemperature;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getCaseTemperature")
    private double lastCaseTemperature;

    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getDeuterium", "getDeuteriumCapacity", "getDeuteriumNeeded",
            "getDeuteriumFilledPercentage"}, docPlaceholder = "deuterium tank")
    public IGasTank deuteriumTank;
    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getTritium", "getTritiumCapacity", "getTritiumNeeded",
            "getTritiumFilledPercentage"}, docPlaceholder = "tritium tank")
    public IGasTank tritiumTank;
    @ContainerSync(tags = FUEL_TAB)
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getDTFuel", "getDTFuelCapacity", "getDTFuelNeeded", "getDTFuelFilledPercentage"}, docPlaceholder = "fuel tank")
    public IGasTank fuelTank;
    @ContainerSync(tags = {"fuel", "heat"}, getter = "getInjectionRate", setter = "setInjectionRate")
    private int injectionRate = 2;

    public double plasmaTemperature;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getHohlraum", docPlaceholder = "Hohlraum slot")
    private final ReactorInventorySlot reactorSlot;

    public ReactorInventorySlot getReactorSlot() {
        return reactorSlot;
    }

    private boolean clientBurning;
    private double clientTemp;

    private AABB deathZone;

    protected int laserShootCountdown = 0;
    protected int laserShootEnergyDuration = 12000;
    public double laserShootMinEnergy = 500000000;
    protected float currentReactivity = 0;
    protected float targetReactivity = 0;
    protected float errorLevel = 0;
    protected float adjustment = 0;
    protected int reactivityUpdateTicks = 10000;
    protected int currentReactivityTick = 0;
    protected int adjustmentTicks = 100;
    protected float difficulty = 10;
    public boolean explodeFlag = false;

    //Target reactivity update rate depends on temperature
    public float getTargetReactivity()
    {
        return targetReactivity;
    }

    /** value in range 0..100 **/
    @ComputerMethod
    public float getEfficiency()
    {
        if(!isBurning()) return 0;
        return (float) Math.min(100, Math.max(0, 1/(Math.abs(targetReactivity - currentReactivity)/100 + 0.2)*22)-10);
    }

    public void setAdjustment(float val)
    {
        adjustment = val;
    }

    public float getAdjustment() {
        return adjustment;
    }

    public float getCurrentReactivity()
    {
        return currentReactivity;
    }

    public void setTargetReactivity(float val)
    {
        targetReactivity = Math.abs(val);
    }

    public void setCurrentReactivity(float val)
    {
        currentReactivity = Math.abs(val);
    }

    @ComputerMethod
    public float getErrorLevel()
    {
        return errorLevel;
    }

    protected void setErrorLevel(float val)
    {
        errorLevel = val;
    }

    public int getLaserShootCountdown()
    {
        return laserShootCountdown;
    }

    protected void setLaserShootCountdown(int val)
    {
        laserShootCountdown = val;
    }

    public void processLaserShoot(FloatingLong laserEnergy)
    {
        if(laserEnergy.greaterOrEqual(FloatingLong.create(laserShootMinEnergy)) && laserShootCountdown == 0) {
            laserShootCountdown = laserShootEnergyDuration;
        }
    }

    protected void laserShootCount()
    {
        if(laserShootCountdown > 0) {
            laserShootCountdown--;
        }
    }

    /** values range 0 .. 5.16 or even bigger **/
    public float getKt()
    {
        //so laser gives you 1 minute of independence from Kt
        if(laserShootEnergyDuration - getLaserShootCountdown() < 1200) {
            return  0;
        }
        float tDevide = 20;
        if(isActiveCooled()) {
            tDevide = 30;
        }
        return (float) Math.pow((float)(Math.abs(Math.sqrt((float)getLastPlasmaTemp()/2000000) - 40))/tDevide, 2);
    }

    // if efficiency bigger than 80% we reducing chances
    protected void updateErrorLevel()
    {
        if(isBurning()) {
            float shift = ((80 - getEfficiency()) * ((getKt() + 1) / 2)) * 0.0005f;
            if(shift > 0) {
                shift = shift*(difficulty/10);
            }
            errorLevel += shift;
        } else {
            errorLevel -= 0.1;
        }

        errorLevel = Math.min(100, Math.max(0, errorLevel));
        if(errorLevel > 0) {
            markDirty();
        }
        if(errorLevel >= 100) {
            currentReactivity = 0;
            targetReactivity = 0;
            adjustment = 0;
            setBurning(false);
            if(BetterFusionReactorConfig.bfr.reactorMeltdown.get()) {
                explodeFlag = true;
            }
        }
    }

    public boolean adjustReactivity(float rate)
    {
        if(adjustment != 0) return false;
        adjustment = rate/(float)adjustmentTicks;
        return true;
    }

    protected void updateAdjustment()
    {
        if(adjustment == 0) return;
        markDirty();
        currentReactivity += adjustment;
        currentReactivity = Math.min(100, Math.max(0, currentReactivity));
        adjustmentTicks--;
        if(adjustmentTicks < 1) {
            adjustmentTicks = 100;
            adjustment = 0;
        }
    }

    public int reactivityUpdateTicksScaled()
    {
        return (int) (( reactivityUpdateTicks / (getKt() + 0.25)) * (difficulty/10));
    }

    public void updateReactivity()
    {
        float low = 0f;
        float high = 100f;
        currentReactivityTick++;
        if(reactivityUpdateTicksScaled() < currentReactivityTick) {
            currentReactivityTick = 0;
            setTargetReactivity(low + new Random().nextFloat() * (high - low));
            setCurrentReactivity((low + new Random().nextFloat() * (high - low) + currentReactivity*3)/4);
        }
    }
    protected boolean isActiveCooled()
    {
        return !fluidTanks.get(0).isEmpty();
    }

    @ComputerMethod(nameOverride = "adjustReactivity")
    public void computerAdjustReactivity(float val) throws ComputerException {
        if(val > 100 || val < -100) {
            throw new ComputerException("Adjustment must be float value in range [-100 .. 100]");
        }
        adjustReactivity(val);
    }



    public BFReactorMultiblockData(TileEntityFusionReactorBlock tile) {
        super(tile);
        if(BetterFusionReactorConfig.bfr.isLoaded()) {
            BetterFusionReactorConfig.bfr.initFusionCoolants();
        }
        //Default biome temp to the ambient temperature at the block we are at
        biomeAmbientTemp = HeatAPI.getAmbientTemp(tile.getLevel(), tile.getTilePos());
        lastPlasmaTemperature = biomeAmbientTemp;
        lastCaseTemperature = biomeAmbientTemp;
        plasmaTemperature = biomeAmbientTemp;
        LongSupplier maxFuel = () -> MAX_FUEL;
        gasTanks.add(deuteriumTank = MultiblockChemicalTankBuilder.GAS.input(this, maxFuel, GeneratorTags.Gases.DEUTERIUM_LOOKUP::contains, this));
        gasTanks.add(tritiumTank = MultiblockChemicalTankBuilder.GAS.input(this, maxFuel, GeneratorTags.Gases.TRITIUM_LOOKUP::contains, this));
        gasTanks.add(fuelTank = MultiblockChemicalTankBuilder.GAS.input(this, maxFuel, GeneratorTags.Gases.FUSION_FUEL_LOOKUP::contains, createSaveAndComparator()));
        gasTanks.add(hotCoolantTank = MultiblockChemicalTankBuilder.GAS.output(this, this::getMaxSteam, gas -> BetterFusionReactorConfig.bfr.allowedCoolantHotGases.contains(gas.getChemical()), this));
        gasTanks.add(gasCoolantTank = MultiblockChemicalTankBuilder.GAS.input(this, this::getMaxCoolant, gas -> BetterFusionReactorConfig.bfr.allowedCoolantGases.contains(gas.getChemical()), this));

        fluidTanks.add(liquidCoolantTank = VariableCapacityFluidTank.input(this, this::getMaxCoolant, fluid -> BetterFusionReactorConfig.bfr.allowedCoolantFluids.contains(fluid.getFluid()), this));

        energyContainers.add(energyContainer = BasicEnergyContainer.output(MAX_ENERGY, this));
        heatCapacitors.add(heatCapacitor = VariableHeatCapacitor.create(caseHeatCapacity, BFReactorMultiblockData::getInverseConductionCoefficient,
                () -> inverseInsulation, () -> biomeAmbientTemp, this));
        inventorySlots.add(reactorSlot = ReactorInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, this, 80, 39));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        for (ValveData data : valves) {
            BlockEntity tile = WorldUtils.getTileEntity(world, data.location);
            if (tile instanceof TileEntityFusionReactorPort) {
                heatHandlers.add((ITileHeatHandler) tile);
            }
        }
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        deathZone = new AABB(getMinPos().getX() + 1, getMinPos().getY() + 1, getMinPos().getZ() + 1,
              getMaxPos().getX(), getMaxPos().getY(), getMaxPos().getZ());
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setDoubleIfPresent(tag, NBTConstants.PLASMA_TEMP, this::setLastPlasmaTemp);
        NBTUtils.setFloatIfPresent(tag, ReactorConstants.NBT_CURRENT_REACTIVITY, this::setCurrentReactivity);
        NBTUtils.setFloatIfPresent(tag, ReactorConstants.NBT_TARGET_REACTIVITY, this::setTargetReactivity);
        NBTUtils.setFloatIfPresent(tag, ReactorConstants.NBT_ERROR_LEVEL, this::setErrorLevel);
        NBTUtils.setFloatIfPresent(tag, ReactorConstants.NBT_ADJUSTMENT, this::setAdjustment);
        NBTUtils.setIntIfPresent(tag, ReactorConstants.NBT_LASER_SHOOT_COUNTDOWN, this::setLaserShootCountdown);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.BURNING, this::setBurning);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.putDouble(NBTConstants.PLASMA_TEMP, getLastPlasmaTemp());
        tag.putFloat(ReactorConstants.NBT_CURRENT_REACTIVITY, getCurrentReactivity());
        tag.putFloat(ReactorConstants.NBT_TARGET_REACTIVITY, getTargetReactivity());
        tag.putFloat(ReactorConstants.NBT_ERROR_LEVEL, getErrorLevel());
        tag.putFloat(ReactorConstants.NBT_ADJUSTMENT, getAdjustment());
        tag.putInt(ReactorConstants.NBT_LASER_SHOOT_COUNTDOWN, getLaserShootCountdown());
        tag.putBoolean(NBTConstants.BURNING, isBurning());
    }

    public void addTemperatureFromEnergyInput(FloatingLong energyAdded) {
        if (isBurning()) {
            setPlasmaTemp(getPlasmaTemp() + energyAdded.divide(plasmaHeatCapacity).doubleValue());
        } else {
            setPlasmaTemp(getPlasmaTemp() + energyAdded.divide(plasmaHeatCapacity).multiply(10).doubleValue());
        }
    }

    private boolean hasHohlraum() {
        if (!reactorSlot.isEmpty()) {
            ItemStack hohlraum = reactorSlot.getStack();
            if (hohlraum.getItem() instanceof ItemHohlraum) {
                Optional<IGasHandler> capability = hohlraum.getCapability(Capabilities.GAS_HANDLER).resolve();
                if (capability.isPresent()) {
                    IGasHandler gasHandlerItem = capability.get();
                    if (gasHandlerItem.getTanks() > 0) {
                        //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
                        return gasHandlerItem.getChemicalInTank(0).getAmount() == gasHandlerItem.getTankCapacity(0);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        //Only thermal transfer happens unless we're hot enough to burn.
        if (getPlasmaTemp() >= burnTemperature) {
            //If we're not burning, yet we need a hohlraum to ignite
            if (!burning && hasHohlraum()) {
                vaporiseHohlraum();
                float low = 0F;
                float high = 100F;
                setCurrentReactivity(low + new Random().nextFloat() * (high - low));
                setTargetReactivity(low + new Random().nextFloat() * (high - low));
            }
            updateErrorLevel();
            //Only inject fuel if we're burning
            if (isBurning()) {
                laserShootCount();
                updateReactivity();
                updateAdjustment();
                injectFuel();
                long fuelBurned = burnFuel();
                if (fuelBurned == 0) {
                    setBurning(false);
                }
            }
        } else {
            currentReactivity = 0;
            targetReactivity = 0;
            adjustment = 0;
            setBurning(false);
        }

        //Perform the heat transfer calculations
        transferHeat();
        updateHeatCapacitors(null);
        updateTemperatures();

        if (isBurning()) {
            kill(world);
        }

        if (
                isBurning() != clientBurning ||
                Math.abs(getLastPlasmaTemp() - clientTemp) > 1_000_000 ||
                getAdjustment() > 0 ||
                getTargetReactivity() != targetReactivity ||
                getErrorLevel() > 0
        ) {
            clientBurning = isBurning();
            clientTemp = getLastPlasmaTemp();
            targetReactivity = getTargetReactivity();
            errorLevel = getErrorLevel();
            needsPacket = true;
        }
        return needsPacket;
    }

    public void updateTemperatures() {
        lastPlasmaTemperature = getPlasmaTemp();
        lastCaseTemperature = heatCapacitor.getTemperature();
    }

    private void kill(Level world) {
        if (world.getRandom().nextInt() % 20 != 0) {
            return;
        }
        List<Entity> entitiesToDie = getWorld().getEntitiesOfClass(Entity.class, deathZone);

        for (Entity entity : entitiesToDie) {
            entity.hurt(entity.damageSources().magic(), 50_000F);
        }
    }

    private void vaporiseHohlraum() {
        ItemStack hohlraum = reactorSlot.getStack();
        Optional<IGasHandler> capability = hohlraum.getCapability(Capabilities.GAS_HANDLER).resolve();
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getTanks() > 0) {
                fuelTank.insert(gasHandlerItem.getChemicalInTank(0), Action.EXECUTE, AutomationType.INTERNAL);
                lastPlasmaTemperature = getPlasmaTemp();
                reactorSlot.setEmpty();
                setBurning(true);
            }
        }
    }

    private void injectFuel() {
        long amountNeeded = fuelTank.getNeeded();
        long amountAvailable = 2 * Math.min(deuteriumTank.getStored(), tritiumTank.getStored());
        long amountToInject = Math.min(amountNeeded, Math.min(amountAvailable, injectionRate));
        amountToInject -= amountToInject % 2;
        long injectingAmount = amountToInject / 2;
        MekanismUtils.logMismatchedStackSize(deuteriumTank.shrinkStack(injectingAmount, Action.EXECUTE), injectingAmount);
        MekanismUtils.logMismatchedStackSize(tritiumTank.shrinkStack(injectingAmount, Action.EXECUTE), injectingAmount);
        fuelTank.insert(GeneratorsGases.FUSION_FUEL.getStack(amountToInject), Action.EXECUTE, AutomationType.INTERNAL);
    }

    private long burnFuel() {
        long fuelBurned = (long) Math.min(fuelTank.getStored(), Math.max(0, lastPlasmaTemperature - burnTemperature) * burnRatio);
        MekanismUtils.logMismatchedStackSize(fuelTank.shrinkStack(fuelBurned, Action.EXECUTE), fuelBurned);
        setPlasmaTemp(getPlasmaTemp() + MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(fuelBurned).divide(plasmaHeatCapacity).doubleValue());
        return fuelBurned;
    }

    private void transferHeat() {
        //Transfer from plasma to casing
        double plasmaCaseHeat = plasmaCaseConductivity * (lastPlasmaTemperature - lastCaseTemperature);
        setPlasmaTemp(getPlasmaTemp() - plasmaCaseHeat / plasmaHeatCapacity);
        heatCapacitor.handleHeat(plasmaCaseHeat);

        //Transfer from casing to water if necessary
        double caseWaterHeat = MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() * (lastCaseTemperature - biomeAmbientTemp);
        int liquidToVaporize = (int) (HeatUtils.getSteamEnergyEfficiency() * caseWaterHeat / HeatUtils.getWaterThermalEnthalpy());
        liquidToVaporize = Math.min(liquidToVaporize, Math.min(liquidCoolantTank.getFluidAmount(), MathUtils.clampToInt(hotCoolantTank.getNeeded())));
        if (liquidToVaporize > 0) {
            for(Gas out: BetterFusionReactorConfig.bfr.coolantMap.keySet()) {
                if(BetterFusionReactorConfig.bfr.coolantMap.get(out).equals(liquidCoolantTank.getFluid().getFluid())) {
                    hotCoolantTank.insert(new GasStack(out, liquidToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
                    MekanismUtils.logMismatchedStackSize(liquidCoolantTank.shrinkStack(liquidToVaporize, Action.EXECUTE), liquidToVaporize);
                    break;
                }
            }
            caseWaterHeat = liquidToVaporize * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
            heatCapacitor.handleHeat(-caseWaterHeat);
        } else {
            long gasToVaporize = (long) (HeatUtils.getSteamEnergyEfficiency() * caseWaterHeat / HeatUtils.getWaterThermalEnthalpy());
            gasToVaporize = Math.min(gasToVaporize, Math.min(gasCoolantTank.getStored(), hotCoolantTank.getNeeded()));
            if(gasToVaporize > 0) {

                for(Gas out: BetterFusionReactorConfig.bfr.coolantMap.keySet()) {
                    if(BetterFusionReactorConfig.bfr.coolantMap.get(out).equals(gasCoolantTank.getType())) {
                        hotCoolantTank.insert(new GasStack(out, gasToVaporize), Action.EXECUTE, AutomationType.INTERNAL);
                        MekanismUtils.logMismatchedStackSize(gasCoolantTank.shrinkStack(gasToVaporize, Action.EXECUTE), gasToVaporize);
                        break;
                    }
                }
                caseWaterHeat = gasToVaporize * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
                heatCapacitor.handleHeat(-caseWaterHeat);
            }
        }

        for (ITileHeatHandler source : heatHandlers) {
            source.simulate();
        }

        //Passive energy generation
        double caseAirHeat = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * (lastCaseTemperature - biomeAmbientTemp);
        heatCapacitor.handleHeat(-caseAirHeat);
        energyContainer.insert(FloatingLong.create(caseAirHeat * MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get()), Action.EXECUTE, AutomationType.INTERNAL);
    }

    public void setLastPlasmaTemp(double temp) {
        lastPlasmaTemperature = temp;
    }

    public double getLastPlasmaTemp() {
        return lastPlasmaTemperature;
    }

    public double getLastCaseTemp() {
        return lastCaseTemperature;
    }

    public double getPlasmaTemp() {
        return plasmaTemperature;
    }

    public void setPlasmaTemp(double temp) {
        if (plasmaTemperature != temp) {
            plasmaTemperature = temp;
            markDirty();
        }
    }

    @ComputerMethod
    public int getInjectionRate() {
        return injectionRate;
    }

    public void setInjectionRate(int rate) {
        if (injectionRate != rate) {
            injectionRate = rate;
            if (getWorld() != null && !isRemote()) {
                if (!liquidCoolantTank.isEmpty()) {
                    liquidCoolantTank.setStackSize(Math.min(liquidCoolantTank.getFluidAmount(), liquidCoolantTank.getCapacity()), Action.EXECUTE);
                }
                if (!gasCoolantTank.isEmpty()) {
                    gasCoolantTank.setStackSize(Math.min(gasCoolantTank.getStored(), gasCoolantTank.getCapacity()), Action.EXECUTE);
                }
                if (!hotCoolantTank.isEmpty()) {
                    hotCoolantTank.setStackSize(Math.min(hotCoolantTank.getStored(), hotCoolantTank.getCapacity()), Action.EXECUTE);
                }
            }
            markDirty();
        }
    }

    public int getMaxCoolant() {
        return MAX_COOLANT * injectionRate;
    }

    public long getMaxSteam() {
        return MAX_STEAM * injectionRate;
    }

    public boolean isBurning() {
        return burning;
    }

    public void setBurning(boolean burn) {
        if (burning != burn) {
            burning = burn;
            markDirty();
        }
    }

    public double getCaseTemp() {
        return heatCapacitor.getTemperature();
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fuelTank.getStored(), fuelTank.getCapacity());
    }

    @ComputerMethod
    public int getMinInjectionRate(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double aMin = burnTemperature * burnRatio * plasmaCaseConductivity * (k + caseAirConductivity) /
                      (MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) -
                       plasmaCaseConductivity * (k + caseAirConductivity));
        return (int) (2 * Math.ceil(aMin / 2D));
    }

    @ComputerMethod
    public double getMaxPlasmaTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        return injectionRate * MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue() / plasmaCaseConductivity *
               (plasmaCaseConductivity + k + caseAirConductivity) / (k + caseAirConductivity);
    }

    @ComputerMethod
    public double getMaxCasingTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        return MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().multiply(injectionRate)
              .divide(k + MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get()).doubleValue();
    }

    @ComputerMethod
    public double getIgnitionTemperature(boolean active) {
        double k = active ? MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() : 0;
        double caseAirConductivity = MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
        double energyPerFusionFuel = MekanismGeneratorsConfig.generators.energyPerFusionFuel.get().doubleValue();
        return burnTemperature * energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) /
               (energyPerFusionFuel * burnRatio * (plasmaCaseConductivity + k + caseAirConductivity) - plasmaCaseConductivity * (k + caseAirConductivity));
    }

    public FloatingLong getPassiveGeneration(boolean active, boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(active);
        return FloatingLong.create(MekanismGeneratorsConfig.generators.fusionThermocoupleEfficiency.get() *
                MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get() * temperature *
                ((getEfficiency()/100+2)/3));
    }

    public long getSteamPerTick(boolean current) {
        double temperature = current ? getLastCaseTemp() : getMaxCasingTemperature(true);
        return MathUtils.clampToLong(HeatUtils.getSteamEnergyEfficiency() *
                MekanismGeneratorsConfig.generators.fusionWaterHeatingRatio.get() *
                (temperature / HeatUtils.getWaterThermalEnthalpy()) *
                ((getEfficiency()/100+2)/3)
        );
    }

    private static double getInverseConductionCoefficient() {
        return 1 / MekanismGeneratorsConfig.generators.fusionCasingThermalConductivity.get();
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "setInjectionRate")
    public void computerSetInjectionRate(int rate) throws ComputerException {
        if (rate < 0 || rate > MAX_INJECTION) {
            //Validate bounds even though we can clamp
            throw new ComputerException("Injection Rate '%d' is out of range must be an even number between 0 and %d. (Inclusive)", rate, MAX_INJECTION);
        } else if (rate % 2 != 0) {
            //Validate it is even
            throw new ComputerException("Injection Rate '%d' must be an even number between 0 and %d. (Inclusive)", rate, MAX_INJECTION);
        }
        setInjectionRate(rate);
    }

    @ComputerMethod
    public FloatingLong getPassiveGeneration(boolean active) {
        return getPassiveGeneration(active, false);
    }

    @ComputerMethod
    public FloatingLong getProductionRate() {
        return getPassiveGeneration(false, false);
    }
    //End computer related methods
}