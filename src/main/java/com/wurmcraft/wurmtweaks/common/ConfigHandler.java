package com.wurmcraft.wurmtweaks.common;

import com.wurmcraft.wurmtweaks.reference.Global;
import com.wurmcraft.wurmtweaks.reference.Local;
import com.wurmcraft.wurmtweaks.utils.InvalidRecipe;
import com.wurmcraft.wurmtweaks.utils.LogHandler;
import com.wurmcraft.wurmtweaks.utils.ReflectionHelper;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber (modid = Global.MODID)
@Config (modid = Global.MODID)
public class ConfigHandler {

	@Config.Comment ("Enable / Disable Debug Mode")
	@Config.LangKey (Local.CONFIG_DEBUG)
	public static boolean debug = false;

	@Config.Comment ("Removes All Crafting Recipes")
	@Config.LangKey (Local.CONFIG_REMOVE_ALL_CRAFTING_RECIPES)
	public static boolean removeAllCraftingRecipes = false;

	@Config.Comment ("Removes All Furnace Recipes")
	@Config.LangKey (Local.CONFIG_REMOVE_ALL_FURNACE_RECIPES)
	public static boolean removeAllFurnaceRecipes = false;

	@Config.Comment ("Copy ItemName To Clipboard via /wt hand Command?")
	@Config.LangKey (Local.CONFIG_COPYITEMNAME)
	public static boolean copyItemName = true;

	@Config.Comment ("URL For Master.js (Master Script)")
	@Config.LangKey (Local.CONFIG_MASTER_SCRIPT)
	public static String masterScript = "https://raw.githubusercontent.com/Wurmcraft/WurmTweaks2/master/scripts/master.ws";

	@Config.Comment ("Check For Script Updates")
	@Config.LangKey (Local.CONFIG_RECIPE_UPDATES)
	public static boolean checkForRecipeUpdates = true;

	@SubscribeEvent
	public static void onConfigChanged (ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID ().equals (Global.MODID)) {
			ConfigManager.load (Global.MODID,Config.Type.INSTANCE);
			LogHandler.info ("Config Saved!");
		}
	}

	public static void handleLateConfigSettings () {
		if (removeAllCraftingRecipes)
			for (IRecipe recipe : ForgeRegistries.RECIPES.getValues ())
				ForgeRegistries.RECIPES.register (new InvalidRecipe (recipe));
		if (removeAllFurnaceRecipes)
			FurnaceRecipes.instance ().getSmeltingList ().clear ();
	}

	@SubscribeEvent
	public void onWorldLoad (WorldEvent.Load e) {
		if (removeAllCraftingRecipes)
			if (!e.getWorld ().isRemote)
				emptyRecipeBook ((WorldServer) e.getWorld ());
	}

	public static void emptyRecipeBook (WorldServer world) {
		try {
			ReflectionHelper.setFinalStatic (world.getAdvancementManager ().getClass ().getDeclaredField ("ADVANCEMENT_LIST"),new AdvancementList ());
		} catch (Exception f) {
			f.printStackTrace ();
		}
	}
}
