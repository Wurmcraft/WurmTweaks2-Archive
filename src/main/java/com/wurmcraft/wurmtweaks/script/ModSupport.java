package com.wurmcraft.wurmtweaks.script;

import com.wurmcraft.wurmtweaks.api.WurmTweaks2API;
import com.wurmcraft.wurmtweaks.script.support.*;
import net.minecraftforge.fml.common.Loader;

public class ModSupport {

	public static void init () {
		WurmScript.register (new Minecraft ());
		if (Loader.isModLoaded ("tconstruct"))
			WurmTweaks2API.register (new TConstruct ());
		if (Loader.isModLoaded ("immersiveengineering"))
			WurmTweaks2API.register (new ImmersiveEngineering ());
		if (Loader.isModLoaded ("extrautils2"))
			WurmTweaks2API.register (new ExtraUtils2 ());
		if (Loader.isModLoaded ("draconicevolution"))
			WurmTweaks2API.register (new DraconicEvolution ());
		if (Loader.isModLoaded ("environmentaltech"))
			WurmTweaks2API.register (new EnvironmentalTech ());
		if (Loader.isModLoaded ("mekanism"))
			WurmTweaks2API.register (new Mekanism ());
		if (Loader.isModLoaded ("techreborn"))
			WurmTweaks2API.register (new TechReborn ());
		if (Loader.isModLoaded ("sonarcore"))
			WurmTweaks2API.register (new SonarCore ());
		if (Loader.isModLoaded ("calculator"))
			WurmTweaks2API.register (new Calculator ());
		if (Loader.isModLoaded ("actuallyadditions"))
			WurmTweaks2API.register (new ActuallyAdditions ());
		if (Loader.isModLoaded ("industrialforegoing"))
			WurmTweaks2API.register (new IndustrialForegoing ());
		if (Loader.isModLoaded ("nuclearcraft"))
			WurmTweaks2API.register (new NuclearCraft ());
		if (Loader.isModLoaded ("betterwithmods"))
			WurmTweaks2API.register (new BetterWithMods ());
		if (Loader.isModLoaded ("abyssalcraft"))
			WurmTweaks2API.register (new AbyssalCraft ());
		if (Loader.isModLoaded ("avaritia"))
			WurmTweaks2API.register (new Avaritia ());
	}
}
