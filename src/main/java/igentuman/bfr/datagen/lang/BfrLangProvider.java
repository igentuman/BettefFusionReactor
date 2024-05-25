package igentuman.bfr.datagen.lang;

import igentuman.bfr.common.BetterFusionReactor;
import igentuman.bfr.common.BfrLang;
import igentuman.bfr.datagen.lang.BaseLanguageProvider;
import igentuman.bfr.common.registries.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

public class BfrLangProvider extends BaseLanguageProvider {

    public BfrLangProvider(PackOutput gen) {
        super(gen, BetterFusionReactor.MODID);
    }

    @Override
    protected void addTranslations() {
        addMisc();
        addBlocks();
        addOres();
    }

    private void addOres() {
        for(String ore : BfrBlocks.ORES) {
            add("block.bfr.irradiated_"+ore+"_ore", "Irradiated "+capitalize(ore)+" Ore");
        }
    }

    public static final String capitalize(String str)
    {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    private void addBlocks() {
        add(BfrBlocks.LASER_FOCUS_MATRIX, "Laser Focus Matrix");
        add(BfrBlocks.FUSION_REACTOR_CONTROLLER, "Fusion Reactor Controller");
        add(BfrBlocks.FUSION_REACTOR_FRAME, "Fusion Reactor Frame");
        add(BfrBlocks.FUSION_REACTOR_LOGIC_ADAPTER, "Fusion Reactor Logic Adapter");
        add(BfrBlocks.FUSION_REACTOR_PORT, "Fusion Reactor Port");
        add(BfrBlocks.IRRADIATOR, "Irradiator");

    }

    private void addMisc() {
        add(BfrLang.LOGIC_OUT_TAB, "Output Signals");
        add(BfrLang.LOGIC_IN_TAB, "Input Signals");
        add(BfrLang.LOGIC_GENERAL_TAB, "General");
        add(BfrLang.REACTOR_ER, "ER");
        add(BfrLang.REACTOR_CR, "CR");
        add(BfrLang.REACTOR_TR, "TR");
        add(BfrLang.REACTOR_EF, "EF");
        add(BfrLang.REACTOR_LASER_READY_BUTTON, "Ready for Laser Impulse");
        add(BfrLang.REACTOR_BUTTON_REACTIVITY_UP, "+5");
        add(BfrLang.REACTOR_BUTTON_REACTIVITY_DOWN, "-5");
        add(BfrLang.REACTOR_HEAT_MULTIPLIER, "Heat Multiplier: %1$s");
        add(BfrLang.REACTOR_CURRENT_REACTIVITY, "Current Reactivity: %1$s");
        add(BfrLang.REACTOR_TARGET_REACTIVITY, "Target Reactivity: %1$s");
        add(BfrLang.REACTOR_EFFICIENCY, "Efficiency: %1$s");
        add(BfrLang.REACTOR_ERROR_LEVEL, "Error Level: %1$s");
        add(BfrLang.EFFICIENCY_TAB, "Efficiency");
        add(BfrLang.REACTOR_LASER_MIN_ENERGY, "Laser Impulse min energy: %1$s");
        add(BfrLang.REACTOR_LASER_MIN_ENERGY_DESCR, "Avoid negative effects of Hm (1 minute)");
        add(BfrLang.REACTOR_HELP1, "Keep EF higher than 80% by adjusting");
        add(BfrLang.REACTOR_HELP2, "CR as close as possible to TR.");
        add(BfrLang.REACTOR_HELP3, "Automate this process with Logic Adapters");
        add(BfrLang.REACTOR_HELP_HEAT_MULTIPLIER1, "Hm affects how often TR changes");
        add(BfrLang.REACTOR_HELP_HEAT_MULTIPLIER2, "It depends on plasma temperature");
        add(BfrLang.REACTOR_HELP_HEAT_MULTIPLIER3, "At some conditions Hm becomes 0");
        add(BfrLang.REACTOR_LOGIC_HELP1, "Logic ports used for automation");
        add(BfrLang.REACTOR_LOGIC_HELP2, "Output signal strength represents value divided by 15");
        add(BfrLang.REACTOR_LOGIC_HELP3, "CR adjustments uses input signal strength value");
        add(BfrLang.REACTOR_LOGIC_HELP4, "Use computers for accurate automation");
        add(BfrLang.EFFICIENCY_GOOD, "Reaction Efficiency is good");
        add(BfrLang.EFFICIENCY_BAD, "Reaction Efficiency is bad");
        add(BfrLang.MIGHT_EXPLODE, "Reactor might explode!");
        add(BfrLang.MIGHT_TURNOFF, "Reactor might turn off!");

        add(BfrLang.IRRADIATOR_DESCRIPTION, "Uses High Energy Particles and Radiation to transform items");

        add(BfrLang.REACTOR_ACTIVE, "Liquid-Cooled");
        add(BfrLang.REACTOR_LOGIC_REACTIVITY_UP, "+ Reactivity");
        add(BfrLang.REACTOR_LOGIC_REACTIVITY_DOWN, "- Reactivity");
        add(BfrLang.REACTOR_LOGIC_INJECTION_DOWN, "-2 Injection Rate");
        add(BfrLang.REACTOR_LOGIC_INJECTION_UP, "+2 Injection Rate");
        add(BfrLang.REACTOR_LOGIC_EFFICIENCY, "Efficiency");
        add(BfrLang.REACTOR_LOGIC_ERROR_LEVEL, "Error Level");
      //Descriptions
        add(BfrLang.DESCRIPTION_REACTOR_ERROR_LEVEL, "100% = full redstone signal strength (15 blocks). 1 block ~ 7%");
        add(BfrLang.DESCRIPTION_REACTOR_EFFICIENCY, "100% = full redstone signal strength (15 blocks). 1 block ~ 7%");
        add(BfrLang.DESCRIPTION_REACTOR_INJECTION_UP, "Input redstone impulse any strength");
        add(BfrLang.DESCRIPTION_REACTOR_INJECTION_DOWN, "Input redstone impulse any strength");
        add(BfrLang.DESCRIPTION_REACTOR_REACTIVITY_DOWN, "Redstone impulse will decrease CR by the strength of impulse (1-15)");
        add(BfrLang.DESCRIPTION_REACTOR_REACTIVITY_UP, "Redstone impulse will increase CR by the strength of impulse (1-15)");

        add(BfrLang.IRRADIATOR_NO_SOURCE, "Radiation source: NOT FOUND");
        add(BfrLang.IRRADIATOR_HAS_SOURCE, "Radiation source: OK");
        add(BfrLang.IRRADIATOR_SOURCE_GUIDE, "Place Irradiator next to reactor port");
        add(BfrLang.IRRADIATOR_FLUX, "Radiative Flux: %1$s");
        add(BfrLang.SOLIDIFIED_WASTE, "Solidified Nuclear Waste");

    }
}