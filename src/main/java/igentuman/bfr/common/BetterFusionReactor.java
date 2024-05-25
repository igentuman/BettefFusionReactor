package igentuman.bfr.common;

import igentuman.bfr.common.events.RadiationEvents;
import igentuman.bfr.common.registries.*;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.lib.Version;
import mekanism.common.lib.multiblock.MultiblockManager;
import igentuman.bfr.common.config.BetterFusionReactorConfig;
import igentuman.bfr.common.content.fusion.BFReactorCache;
import igentuman.bfr.common.content.fusion.BFReactorMultiblockData;
import igentuman.bfr.common.content.fusion.BFReactorValidator;
import igentuman.bfr.common.network.BfrPacketHandler;
import igentuman.bfr.common.registries.BfrBuilders.FusionReactorBuilder;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterFusionReactor.MODID)
public class BetterFusionReactor implements IModModule {

    public static final String MODID = "bfr";

    public static BetterFusionReactor instance;

    /**
     * BetterFusionReactor version number
     */
    public final Version versionNumber;
    /**
     * Mekanism Generators Packet Pipeline
     */
    private final BfrPacketHandler packetHandler;

    public static final MultiblockManager<BFReactorMultiblockData> fusionReactorManager = new MultiblockManager<>("fusionReactor", BFReactorCache::new, BFReactorValidator::new);
    public BetterFusionReactor() {
        Mekanism.modulesLoaded.add(instance = this);
        BetterFusionReactorConfig.registerConfigs(ModLoadingContext.get());
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::imcQueue);
        BfrBlocks.BLOCKS.register(modEventBus);
        BfrItems.ITEMS.register(modEventBus);
        BfrCreativeTabs.BFR_TAB.register(modEventBus);
        BfrContainerTypes.CONTAINER_TYPES.register(modEventBus);
        BfrTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        BfrModules.MODULES.register(modEventBus);
        BfrRecipes.init();

        //Set our version number to match the mods.toml file, which matches the one in our build.gradle
        versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
        packetHandler = new BfrPacketHandler();
    }

    public static BfrPacketHandler packetHandler() {
        return instance.packetHandler;
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(BetterFusionReactor.MODID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        packetHandler.initialize();
        MinecraftForge.EVENT_BUS.register(new RadiationEvents());
        event.enqueueWork(() -> {
            BuildCommand.register("fusion", GeneratorsLang.FUSION_REACTOR, new FusionReactorBuilder());
        });



        //Finalization
        Mekanism.logger.info("Loaded 'Better Fusion Reactor' module.");
    }

    private void imcQueue(InterModEnqueueEvent event) {
    }

    @Override
    public Version getVersion() {
        return versionNumber;
    }

    @Override
    public String getName() {
        return "Better Fusion Reactor";
    }

    @Override
    public void resetClient() {

    }

    private void onConfigLoad(ModConfigEvent configEvent) {
        //Note: We listen to both the initial load and the reload, to make sure that we fix any accidentally
        // cached values from calls before the initial loading
        ModConfig config = configEvent.getConfig();
        //Make sure it is for the same modid as us
        if (config.getModId().equals(MODID) && config instanceof MekanismModConfig) {
            ((MekanismModConfig) config).clearCache(configEvent);
        }
    }
}