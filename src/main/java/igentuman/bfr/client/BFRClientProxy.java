package igentuman.bfr.client;

import igentuman.bfr.client.gui.*;
import igentuman.bfr.client.render.RenderReactor;
import igentuman.bfr.common.block.states.BlockStateReactor;
import igentuman.bfr.common.tile.reactor.TileEntityReactorController;
import igentuman.bfr.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.client.render.MekanismRenderer;
import igentuman.bfr.common.BFRBlocks;
import igentuman.bfr.common.BFRCommonProxy;
import igentuman.bfr.common.BFR;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BFRClientProxy extends BFRCommonProxy {

    private static final IStateMapper reactorMapper = new BlockStateReactor.ReactorBlockStateMapper();

    @Override
    public void registerTESRs() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorController.class, new RenderReactor());
    }


    @Override
    public void registerBlockRenders() {
        ModelLoader.setCustomStateMapper(BFRBlocks.Reactor, reactorMapper);
        ModelLoader.setCustomStateMapper(BFRBlocks.ReactorGlass, reactorMapper);

        for (BlockStateReactor.ReactorBlockType type : BlockStateReactor.ReactorBlockType.values()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(type.blockType.getBlock()), type.meta, new ModelResourceLocation(new ResourceLocation(BFR.MODID, type.getName()), "inventory"));
        }
    }

    public void registerItemRender(Item item) {
        MekanismRenderer.registerItemRender(BFR.MODID, item);
    }

    @Override
    public void preInit(FMLPreInitializationEvent preEvent) {
        super.preInit(preEvent);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public GuiScreen getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof mekanism.generators.common.tile.reactor.TileEntityReactorController) {
            return (GuiScreen) MekanismGenerators.proxy.getClientGui(ID, player, world, pos);
        }
        switch (ID) {
            case 10:
                return new GuiReactorController(player.inventory, (TileEntityReactorController) tileEntity);
            case 11:
                return new GuiReactorHeat(player.inventory, (TileEntityReactorController) tileEntity);
            case 12:
                return new GuiReactorFuel(player.inventory, (TileEntityReactorController) tileEntity);
            case 13:
                return new GuiReactorStats(player.inventory, (TileEntityReactorController) tileEntity);
            case 15:
                return new GuiReactorLogicAdapter(player.inventory, (TileEntityReactorLogicAdapter) tileEntity);
            case 69:
                return new GuiReactorEfficiency(player.inventory, (TileEntityReactorController) tileEntity);
            case 70:
                return new GuiReactorLogicAdapterInput(player.inventory, (TileEntityReactorLogicAdapter) tileEntity);
            case 71:
                return new GuiReactorLogicAdapterOutput(player.inventory, (TileEntityReactorLogicAdapter) tileEntity);
        }

        return (GuiScreen) MekanismGenerators.proxy.getClientGui(ID, player, world, pos);
    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre event) {
    }
}