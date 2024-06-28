package igentuman.bfr.common;

import igentuman.bfr.common.config.BfrConfig;
import igentuman.bfr.common.config.ReactorCoolantRecipesConfig;
import igentuman.bfr.common.inventory.container.ContainerReactorController;
import igentuman.bfr.common.recipes.BFRRecipes;
import mekanism.common.base.IGuiProvider;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import igentuman.bfr.common.tile.reactor.TileEntityReactorController;
import igentuman.bfr.common.tile.reactor.TileEntityReactorFrame;
import igentuman.bfr.common.tile.reactor.TileEntityReactorGlass;
import igentuman.bfr.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import igentuman.bfr.common.tile.reactor.TileEntityReactorLogicAdapter;
import igentuman.bfr.common.tile.reactor.TileEntityReactorPort;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;


@Mod.EventBusSubscriber(modid = BFR.MODID)
public class BFRCommonProxy implements IGuiProvider {

    private static void registerTileEntity(Class<? extends TileEntity> clazz, String name) {
        GameRegistry.registerTileEntity(clazz, new ResourceLocation(BFR.MODID, name));
    }

    /**
     * Register normal tile entities
     */
    public void registerTileEntities() {
        registerTileEntity(TileEntityReactorController.class, "reactor_controller");
        registerTileEntity(TileEntityReactorFrame.class, "reactor_frame");
        registerTileEntity(TileEntityReactorGlass.class, "reactor_glass");
        registerTileEntity(TileEntityReactorLaserFocusMatrix.class, "reactor_laser_focus");
        registerTileEntity(TileEntityReactorLogicAdapter.class, "reactor_logic_adapter");
        registerTileEntity(TileEntityReactorPort.class, "reactor_port");
    }

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }
    public void preInit(FMLPreInitializationEvent preEvent) {
        BfrConfig.preInit();
        ReactorCoolantRecipesConfig.preInit();
        MinecraftForge.EVENT_BUS.register(new BFRRecipes());
    }

    public void loadConfiguration() {

    }

    @Override
    public Object getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        return null;
    }

    @Override
    public Container getServerGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof mekanism.generators.common.tile.reactor.TileEntityReactorController) {
            return MekanismGenerators.proxy.getServerGui(ID, player, world, pos);
        }
        switch (ID) {
            case 10:
                return new ContainerReactorController(player.inventory, (TileEntityReactorController) tileEntity);
            case 11:
            case 12:
            case 13:
            case 69:
            case 70:
            case 71:
            case 15:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
        }
        return MekanismGenerators.proxy.getServerGui(ID, player, world, pos);
    }
}