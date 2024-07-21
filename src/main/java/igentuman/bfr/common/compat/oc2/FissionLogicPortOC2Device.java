package igentuman.bfr.common.compat.oc2;

import li.cil.oc2r.api.bus.device.object.Callback;
import li.cil.oc2r.api.bus.device.object.NamedDevice;
import li.cil.oc2r.api.bus.device.object.ObjectDevice;
import li.cil.oc2r.api.bus.device.rpc.RPCDevice;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class FissionLogicPortOC2Device {

    public static RPCDevice createDevice(TileEntityFissionReactorLogicAdapter blockEntity) {
        return new ObjectDevice(new BfrOC2ReactorDeviceRecord(blockEntity));
    }

    public record BfrOC2ReactorDeviceRecord(TileEntityFissionReactorLogicAdapter reactorPort) implements NamedDevice {

        @Callback
        public Object[] getFuel() {
            return new Object[]{reactorPort.getMultiblock().fuelTank.getStack().getType().toString(), reactorPort.getMultiblock().fuelTank.getStack().getAmount()};
        }

        @Callback
        public long getDamagePercent() {
            return reactorPort.getMultiblock().getDamagePercent();
        }

        @Callback
        public void activate() {
            reactorPort.getMultiblock().setActive(true);
        }

        @Callback
        public void scram() {
            reactorPort.getMultiblock().setActive(false);
        }

        @Callback
        public void setBurnRate(double val) {
            long max = reactorPort.getMultiblock().getMaxBurnRate();
            val = Math.min(val, max);
            val = Math.max(val, 0);
            reactorPort.getMultiblock().setRateLimit(val);
        }

        @Callback
        public long getMaxBurnRate() {
            return reactorPort.getMultiblock().getMaxBurnRate();
        }

        @Callback
        public boolean isActive() {
            return reactorPort.getMultiblock().isActive();
        }

        @Callback
        public boolean isForceDisabled() {
            return reactorPort.getMultiblock().isForceDisabled();
        }

        @Callback
        public double getHeatingRate() {
            return reactorPort.getMultiblock().lastBoilRate;
        }

        @Callback
        public double getTemperature() {
            return reactorPort.getMultiblock().heatCapacitor.getTemperature();
        }

        @Callback
        public double getFuelSurfaceArea() {
            return reactorPort.getMultiblock().surfaceArea;
        }

        @Callback
        public double getEnvironmentalLoss() {
            return reactorPort.getMultiblock().lastEnvironmentLoss;
        }

        @Callback
        public double getBurnRate() {
            return reactorPort.getMultiblock().rateLimit;
        }

        @Callback
        public double getActualBurnRate() {
            return reactorPort.getMultiblock().lastBurnRate;
        }

        @Callback
        public double getBoilEfficiency() {
            return reactorPort.getMultiblock().getBoilEfficiency();
        }

        @Callback
        public Object[] getWaste() {
            return new Object[]{reactorPort.getMultiblock().wasteTank.getStack().getType().toString(), reactorPort.getMultiblock().wasteTank.getStack().getAmount()};
        }

        @Callback
        public Object[] getCoolant() {
            return new Object[]{reactorPort.getMultiblock().gasCoolantTank.getStack().getType().toString(), reactorPort.getMultiblock().gasCoolantTank.getStack().getAmount()};
        }

        @Callback
        public Object[] getLiquidCoolant() {
            return new Object[]{reactorPort.getMultiblock().fluidCoolantTank.getFluid().getFluid().toString(), reactorPort.getMultiblock().fluidCoolantTank.getFluid().getAmount()};
        }

        @Callback
        public Object[] getHeatedCoolant() {
            return new Object[] {reactorPort.getMultiblock().heatedCoolantTank.getStack().getType().toString(), reactorPort.getMultiblock().heatedCoolantTank.getStack().getAmount()};
        }

        @Override
        public @NotNull Collection<String> getDeviceTypeNames() {
            return singletonList("fission_reactor_logic_port");
        }
    }
}
