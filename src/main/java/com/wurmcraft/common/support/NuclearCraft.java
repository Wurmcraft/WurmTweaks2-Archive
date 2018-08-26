package com.wurmcraft.common.support;

import com.wurmcraft.api.script.anotations.FinalizeSupport;
import com.wurmcraft.api.script.anotations.InitSupport;
import com.wurmcraft.api.script.anotations.ScriptFunction;
import com.wurmcraft.api.script.anotations.Support;
import com.wurmcraft.common.ConfigHandler;
import com.wurmcraft.common.script.ScriptExecutor;
import com.wurmcraft.common.support.utils.Converter;
import nc.recipe.NCRecipes;
import net.minecraft.item.ItemStack;
import org.cliffc.high_scale_lib.NonBlockingHashSet;

@Support(modid = "nuclearcraft")
public class NuclearCraft {

  private NonBlockingHashSet<Object[]> manufactory;
  private NonBlockingHashSet<Object[]> separator;
  private NonBlockingHashSet<Object[]> pressurizer;


  @InitSupport
  public void init() {
    if (manufactory == null) {
      manufactory = new NonBlockingHashSet<>();
      separator = new NonBlockingHashSet<>();
      pressurizer = new NonBlockingHashSet<>();
    }
    if (ConfigHandler.removeAllMachineRecipes) {
      NCRecipes.Type.PRESSURIZER.getRecipeHandler().recipes.clear();
      NCRecipes.Type.ISOTOPE_SEPARATOR.getRecipeHandler().recipes.clear();
      NCRecipes.Type.MANUFACTORY.getRecipeHandler().recipes.clear();
      NCRecipes.Type.ALLOY_FURNACE.getRecipeHandler().recipes.clear();
      NCRecipes.Type.CHEMICAL_REACTOR.getRecipeHandler().recipes.clear();
      NCRecipes.Type.SUPERCOOLER.getRecipeHandler().recipes.clear();
      NCRecipes.Type.INFUSER.getRecipeHandler().recipes.clear();
      NCRecipes.Type.INGOT_FORMER.getRecipeHandler().recipes.clear();
      NCRecipes.Type.MELTER.getRecipeHandler().recipes.clear();
    } else if (ScriptExecutor.reload) {
      manufactory.clear();
      separator.clear();
      pressurizer.clear();
      // TODO Remove on Reload
    }
  }

  @FinalizeSupport
  public void finishSupport() {
    for (Object[] recipe : manufactory) {
      NCRecipes.Type.MANUFACTORY.getRecipeHandler()
          .addRecipe((ItemStack) recipe[0], (ItemStack) recipe[1], (int) recipe[2]);
    }
    for (Object[] recipe : separator) {
      NCRecipes.Type.ISOTOPE_SEPARATOR.getRecipeHandler()
          .addRecipe((ItemStack) recipe[0], (ItemStack) recipe[1], (ItemStack) recipe[2],
              (int) recipe[3]);
    }
    for (Object[] recipe : pressurizer) {
      NCRecipes.Type.PRESSURIZER.getRecipeHandler()
          .addRecipe((ItemStack) recipe[0], (ItemStack) recipe[1], (int) recipe[2]);
    }
  }

  @ScriptFunction(modid = "nuclearcraft", inputFormat = "ItemStack ItemStack Integer")
  public void addManufactory(Converter converter, String[] line) {
    manufactory.add(new Object[]{converter.convert(line[1]), converter.convert(line[0]),
        Integer.parseInt(line[2])});
  }

  @ScriptFunction(modid = "nuclearcraft", inputFormat = "ItemStack ItemStack ItemStack Integer")
  public void addIsotopeSeparator(Converter converter, String[] line) {

    separator.add(new Object[]{converter.convert(line[2]), converter.convert(line[0]),
        converter.convert(line[1]), Integer.parseInt(line[3])});
  }

  @ScriptFunction(modid = "nuclearcraft", inputFormat = "ItemStack ItemStack Integer")
  public void addPressurizer(Converter converter, String[] line) {
    pressurizer.add(new Object[]{converter.convert(line[1]), converter.convert(line[0]),
        Integer.parseInt(line[2])});
  }
}
