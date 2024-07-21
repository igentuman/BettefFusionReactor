package igentuman.bfr.common.compat.oc2;

import igentuman.bfr.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import li.cil.oc2r.api.bus.device.Device;
import li.cil.oc2r.api.bus.device.object.Callback;
import li.cil.oc2r.api.bus.device.object.NamedDevice;
import li.cil.oc2r.api.bus.device.object.ObjectDevice;
import li.cil.oc2r.api.bus.device.rpc.RPCDevice;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class FusionLogicPortOC2Device {

    public static final Capability<Device> DEVICE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static RPCDevice createDevice(TileEntityFusionReactorLogicAdapter blockEntity) {
        return new ObjectDevice(new BfrOC2ReactorDeviceRecord(blockEntity));
    }

    public record BfrOC2ReactorDeviceRecord(TileEntityFusionReactorLogicAdapter reactorPort) implements NamedDevice {

        @Callback
        public int getInjectionRate() {
            return reactorPort.getMultiblock().getInjectionRate();
        }

        @Callback
        public void setInjectionRate(int rate) {
            reactorPort.getMultiblock().setInjectionRate(rate);
        }


        @Callback
        public ItemStack getHohlraum() {
            return reactorPort.getMultiblock().getReactorSlot().getStack();
        }

        @Callback
        public float getEfficiency() {
            return reactorPort.getMultiblock().getEfficiency();
        }

        @Callback
        public void adjustReactivity(float val) {
            reactorPort.getMultiblock().adjustReactivity(val);
        }


        @Callback
        public int getMinInjectionRate(boolean active) {
            return reactorPort.getMultiblock().getMinInjectionRate(active);
        }


        @Callback
        public double getMaxPlasmaTemperature(boolean active) {
            return reactorPort.getMultiblock().getMaxPlasmaTemperature(active);
        }


        @Callback
        public double getMaxCasingTemperature(boolean active) {
            return reactorPort.getMultiblock().getMaxCasingTemperature(active);
        }


        @Callback
        public double getIgnitionTemperature(boolean active) {
            return reactorPort.getMultiblock().getIgnitionTemperature(active);
        }

        @Callback
        public long getPassiveGeneration(boolean active) {
            return reactorPort.getMultiblock().getPassiveGeneration(active).getValue();
        }


        @Callback
        public long getProductionRate() {
            return reactorPort.getMultiblock().getProductionRate().getValue();
        }

        @Callback
        public Object[] getCoolant() {
            return new Object[]{reactorPort.getMultiblock().gasCoolantTank.getStack().getType().toString(), reactorPort.getMultiblock().gasCoolantTank.getStack().getAmount()};
        }


        @Callback
        public Object[] getLiquidCoolant() {
            return new Object[]{reactorPort.getMultiblock().liquidCoolantTank.getFluid().getFluid().toString(), reactorPort.getMultiblock().liquidCoolantTank.getFluid().getAmount()};
        }

        @Callback
        public Object[] getSteam() {
            return new Object[]{reactorPort.getMultiblock().hotCoolantTank.getStack().getType().toString(), reactorPort.getMultiblock().hotCoolantTank.getStack().getAmount()};
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList("fusion_reactor_logic_port");
        }
    }
}
