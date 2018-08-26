package com.wurmcraft.common.support;

import com.wurmcraft.api.script.anotations.FinalizeSupport;
import com.wurmcraft.api.script.anotations.InitSupport;
import com.wurmcraft.api.script.anotations.ScriptFunction;
import com.wurmcraft.api.script.anotations.Support;
import com.wurmcraft.common.ConfigHandler;
import com.wurmcraft.common.script.ScriptExecutor;
import com.wurmcraft.common.support.utils.Converter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.cliffc.high_scale_lib.NonBlockingHashSet;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.DryingRecipe;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.smeltery.AlloyRecipe;
import slimeknights.tconstruct.library.smeltery.Cast;
import slimeknights.tconstruct.library.smeltery.ICastingRecipe;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;
import slimeknights.tconstruct.library.tinkering.MaterialItem;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerMaterials;

@Support(modid = "tconstruct")
public class TConstruct {

  private static int INGOT = ConfigHandler.tinkersConstructIngotAmount;
  private static int BLOCK = ConfigHandler.tinkersConstructBlockMultiplier * INGOT;

  private NonBlockingHashSet<Object[]> casting;
  private NonBlockingHashSet<Object[]> basin;
  private NonBlockingHashSet<Object[]> alloy;
  private NonBlockingHashSet<Object[]> drying;
  private NonBlockingHashSet<Object[]> fuel;
  private NonBlockingHashSet<MeltingRecipe> melting;
  private NonBlockingHashSet<Object[]> entityMelting;

  @InitSupport
  public void init() {
    if (casting == null) {
      casting = new NonBlockingHashSet<>();
      basin = new NonBlockingHashSet<>();
      alloy = new NonBlockingHashSet<>();
      drying = new NonBlockingHashSet<>();
      fuel = new NonBlockingHashSet<>();
      melting = new NonBlockingHashSet<>();
      entityMelting = new NonBlockingHashSet<>();
    }
    if (ConfigHandler.removeAllMachineRecipes) {
      try {
        Field tableCasting = TinkerRegistry.class.getDeclaredField("tableCastRegistry");
        tableCasting.setAccessible(true);
        ((List<ICastingRecipe>) tableCasting.get(TinkerRegistry.getAllTableCastingRecipes()))
            .clear();
        Field melting = TinkerRegistry.class.getDeclaredField("meltingRegistry");
        melting.setAccessible(true);
        ((List<MeltingRecipe>) melting.get(TinkerRegistry.getAllMeltingRecipies())).clear();
        Field basinCast = TinkerRegistry.class.getDeclaredField("basinCastRegistry");
        basinCast.setAccessible(true);
        ((List<ICastingRecipe>) basinCast.get(TinkerRegistry.getAllBasinCastingRecipes())).clear();
        Field alloy = TinkerRegistry.class.getDeclaredField("alloyRegistry");
        alloy.setAccessible(true);
        ((List<AlloyRecipe>) alloy.get(TinkerRegistry.getAlloys())).clear();
        Field drying = TinkerRegistry.class.getDeclaredField("dryingRegistry");
        drying.setAccessible(true);
        ((List<DryingRecipe>) drying.get(TinkerRegistry.getAllDryingRecipes())).clear();
      } catch (IllegalAccessException | NoSuchFieldException e) {
        e.printStackTrace();
      }
    } else if (ScriptExecutor.reload) {
      casting.clear();
      basin.clear();
      alloy.clear();
      drying.clear();
      fuel.clear();
      melting.clear();
      entityMelting.clear();
      // TODO Remove Old Recipes
    }
  }

  @FinalizeSupport
  public void finishSupport() {
    for (Object[] recipe : casting) {
      TinkerRegistry
          .registerTableCasting((ItemStack) recipe[0], (ItemStack) recipe[1], (Fluid) recipe[2],
              (int) recipe[3]);
    }
    for (Object[] recipe : basin) {
      TinkerRegistry
          .registerBasinCasting((ItemStack) recipe[0], (ItemStack) recipe[1], (Fluid) recipe[2],
              (int) recipe[3]);
    }
    for (Object[] recipe : alloy) {
      TinkerRegistry.registerAlloy((FluidStack) recipe[0], (FluidStack[]) recipe[1]);
    }
    for (Object[] recipe : drying) {
      TinkerRegistry
          .registerDryingRecipe((ItemStack) recipe[0], (ItemStack) recipe[1], (int) recipe[2]);
    }
    for (Object[] recipe : fuel) {
      TinkerRegistry.registerSmelteryFuel((FluidStack) recipe[0], (int) recipe[1]);
    }
    melting.forEach(recipe -> TinkerRegistry.registerMelting(recipe));
    for (Object[] recipe : entityMelting) {
      TinkerRegistry
          .registerEntityMelting((Class<? extends Entity>) recipe[0], (FluidStack) recipe[1]);
    }
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "ItemStack ItemStack FluidStack")
  public void addCasting(Converter converter, String[] line) {
    casting.add(new Object[]{converter.convert(line[0]), converter.convert(line[1]),
        converter.convert(line[2]), ((FluidStack) converter.convert(line[2])).amount});
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "ItemStack ItemStack FluidStack")
  public void addBasin(Converter converter, String[] line) {
    basin.add(new Object[]{converter.convert(line[0]), converter.convert(line[1]),
        converter.convert(line[2]), ((FluidStack) converter.convert(line[2])).amount});
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "FluidStack FluidStack ...")
  public void addAlloy(Converter converter, String[] line) {
    List<FluidStack> lineFluids = new ArrayList<>();
    for (int index = 1; index < line.length; index++) {
      lineFluids.add((FluidStack) converter.convert(line[index]));
    }
    alloy.add(new Object[]{converter.convert(line[0]), lineFluids.toArray(new FluidStack[0])});
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "ItemStack ItemStack Integer")
  public void addDrying(Converter converter, String[] line) {
    drying.add(new Object[]{converter.convert(line[0]), converter.convert(line[1]),
        Integer.parseInt(line[2])});
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "FluidStack Integer")
  public void addFuel(Converter converter, String[] line) {
    fuel.add(new Object[]{converter.convert(line[0]), Integer.parseInt(line[1])});
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "FluidStack ItemStack Integer")
  public void addMelting(Converter converter, String[] line) {
    melting.add(new MeltingRecipe(RecipeMatch.of((ItemStack) converter.convert(line[1])),
        ((FluidStack) converter.convert(line[0])), Integer.parseInt(line[2])));
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "FluidStack Entity")
  public void addEntityMelting(Converter converter, String[] line) {
    entityMelting.add(new Object[]{((EntityLiving) converter.convert(line[0], 1)).getClass(),
        converter.convert(line[0])});
  }
  // End Of Normal Mod Support

  @ScriptFunction(modid = "tconstruct", inputFormat = "FluidStack ItemStack Block")
  public void handleMelting(Converter converter, String[] line) {
    Fluid fluid = ((FluidStack) converter.convert(line[0], 1)).getFluid();
    if (converter.convert(line[2]) != ItemStack.EMPTY) {
      melting.add(
          new MeltingRecipe(RecipeMatch.of((ItemStack) converter.convert(line[2])), fluid, BLOCK));
      basin.add(new Object[]{converter.convert(line[2]), ItemStack.EMPTY, fluid, BLOCK});
    }
    if (converter.convert(line[1]) != ItemStack.EMPTY) {
      melting.add(
          new MeltingRecipe(RecipeMatch.of((ItemStack) converter.convert(line[1])), fluid, INGOT));
      casting.add(new Object[]{converter.convert(line[1]),
          converter.convert("<tconstruct:cast_custom>"), fluid, INGOT});
    }
  }

  @ScriptFunction(modid = "tconstruct", inputFormat = "String FluidStack")
  public void handleMaterialParts(Converter converter, String[] line) {
    Fluid fluid = ((FluidStack) converter.convert(line[1])).getFluid();
    Material material = null;
    for (Material mat : TinkerMaterials.materials) {
      if (mat.getIdentifier().equalsIgnoreCase(line[0])) {
        material = mat;
      }
    }
    for (IToolPart toolPart : TinkerRegistry.getToolParts()) {
      if (toolPart instanceof MaterialItem && toolPart.canBeCasted()) {
        ItemStack stack = toolPart.getItemstackWithMaterial(material);
        melting.add(new MeltingRecipe(RecipeMatch.of(stack), fluid, normalize(toolPart.getCost())));
        ItemStack cast = new ItemStack(TinkerSmeltery.cast);
        Cast.setTagForPart(cast, stack.getItem());
        casting.add(new Object[]{stack, cast, fluid, (normalize(toolPart.getCost()))});
      }
    }
  }

  private int normalize(int amount) {
    if (amount == Material.VALUE_Ingot) {
      return INGOT;
    } else if (amount == Material.VALUE_Block) {
      return BLOCK;
    } else if (amount == Material.VALUE_Nugget) {
      return INGOT / 9;
    } else if (amount == Material.VALUE_Shard) {
      return INGOT / 2;
    }
    return INGOT * ((amount / Material.VALUE_Ingot));
  }
}
