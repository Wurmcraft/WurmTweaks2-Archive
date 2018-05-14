package com.wurmcraft.script.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wurmcraft.script.utils.StackSettings.*;

//TODO a lot of these methods should be static...

/**
 * Converts Items, Ingredents and Ore Dictionary into a usable form
 */
public class StackHelper {

 private final Thread mainThread;
 public HashMap<String, ItemStack> cacheItems = new HashMap<>();
 private boolean cache;

 /**
  * @param useCache Create a cache to improve preformance
  */
 public StackHelper(boolean useCache) {
  this.mainThread = Thread.currentThread();
  this.cache = useCache;
 }

 /**
  * Converts an String item into an ItemStack for use in recipes
  *
  * @param item String to convert back into an Item
  * @return ItemStack version of the item
  */
 public ItemStack convert(String item) {
  if (cache && cacheItems.containsKey(item)) return cacheItems.get(item);
  if (item.equals(EMPTY_STACK.getFormatting())) return ItemStack.EMPTY;
  if (!item.isEmpty() && item.startsWith(FRONT.getFormatting()) && item.endsWith(BACK.getFormatting())) {
   if (isOreEntry(item)) {
    //TODO Log
    System.out.println("[Debug]: OreDictionary was converted to ItemStack, this may cause issues with the recipe!");
    return getOreItems(item).get(0); // Use an Ingredient instead of an ItemStack to allow for full OreDict Support
   } else if (item.contains(":") && item.contains(STACK_SIZE.getFormatting()) && item.length() >= 9) {
    item = item.replaceAll(SPACE.getFormatting(), " ");
    String resourcePath;
    if (item.contains(META.getFormatting())) {
     resourcePath = item.substring(item.indexOf(":") + 1, item.indexOf(META.getFormatting()));
    } else {
     resourcePath = item.substring(item.indexOf(":") + 1, item.indexOf(BACK.getFormatting()));
    }
    ResourceLocation itemLookup =
     new ResourceLocation(item.substring(item.indexOf(STACK_SIZE.getFormatting()) + 1, item.indexOf(":")), resourcePath);
    Item validItem = getItem(itemLookup);
    if (validItem != null) {
     int stackSize = Integer.valueOf(item.substring(1, item.indexOf(STACK_SIZE.getFormatting())));
     int meta = 0;
     if (item.contains("@")) {
      int NBTSub;
      if (item.contains(NBT.getFormatting())) {
       NBTSub = item.indexOf(NBT.getFormatting());
      } else {
       NBTSub = item.indexOf(BACK.getFormatting());
      }
      meta = Integer.valueOf(item.substring(item.indexOf(META.getFormatting()) + 1, NBTSub));
     }
     ItemStack stack = new ItemStack(validItem, stackSize, meta);
     if (item.contains(NBT.getFormatting())) {
      try {
       stack.setTagCompound(JsonToNBT.getTagFromJson(item.substring(item.indexOf(NBT.getFormatting()) + 1, item.length() - 1)));
      } catch (NBTException e) {
       System.out.println("Invalid NBT '" + item.substring(item.indexOf(NBT.getFormatting()) + 1, item.length() - 1));
      }
      if (cache) cacheItems.put(item, stack);
      return stack;
     }
     return stack;
    }
   }
  }
  System.err.println("ERROR GETTING ITEMSTACK!");
  return null;
 }

 /**
  * Converts an String item into an Ingredient for use in recipes
  *
  * @param item Item / OreEntry to convert to Ingredent
  * @return Ingredent form of the item
  */
 public Ingredient convertIngredient(String item) {
  if (isOreEntry(item)) {
   return new IngredientWrapper(getOreItems(item).toArray(new ItemStack[0]));
  }
  return new IngredientWrapper(convert(item));
 }

 /**
  * Converts an String FluidStack into one for use in recipes
  *
  * @param fluid String FluidStack to convert
  * @return FluidStack version of the String
  */
 public FluidStack convertFluid(String fluid) {
  if (fluid.startsWith(FRONT.getFormatting() + FLUID.getFormatting()) && fluid.endsWith(BACK.getFormatting())) {
   Fluid fluidType = getFluid(fluid.substring(fluid.indexOf(StackSettings.STACK_SIZE.getFormatting()) + 1, fluid.length() - 1));
   int amount = Integer.parseInt(fluid.substring(2, fluid.indexOf(StackSettings.STACK_SIZE.getFormatting()) - 1));
   return new FluidStack(fluidType, amount);
  }
  return null;
 }

 /**
  * Converts / Parses to get ItemStack or String ready for use within the scripting system
  *
  * @param stack ItemStack or String to convert to be used within the scripting system
  */
 public String convert(Object stack) {
  if (stack instanceof String) {
   String item = (String)stack;
   if (item.length() >= 3 && isOreEntry(item)) {
    return item.contains(FRONT.getFormatting()) ? item : FRONT + item + BACK;
   }
   return item;
  } else if (stack instanceof ItemStack) {
   return convertToString((ItemStack)stack);
  } else if (((Ingredient)stack).getMatchingStacks().length > 0) {
   return convertToString(((Ingredient)stack).getMatchingStacks()[0]);
  }
  return "";
 }

 /**
  * Converts a ItemStack into its String version
  *
  * @param stack Stack to convert to a String
  * @return String version of the ItemStack
  */
 private String convertToString(ItemStack stack) {
  String temp =
   FRONT.getFormatting() +
    stack.getCount() +
    STACK_SIZE.getFormatting() +
    stack.getItem().getRegistryName().getResourceDomain() +
    ":" +
    stack.getItem().getRegistryName().getResourcePath();

  if (stack.getItemDamage() > 0) temp = temp + META + stack.getItemDamage();
  if (stack.hasTagCompound()) {
   return temp.replaceAll(" ", SPACE.getFormatting()) + NBT.getFormatting() + stack.getTagCompound() + BACK.getFormatting();
  }
  return temp.replaceAll(" ", SPACE.getFormatting()) + BACK.getFormatting();
 }


 /**
  * Checks if a String is a valid OreDict entry
  *
  * @param entry Entry to check
  * @return If entry is a valid OreDict entry
  */
 public boolean isOreEntry(String entry) {
  return
   !entry.isEmpty() &&
    entry.length() > 3 &&
    entry.startsWith(FRONT.getFormatting()) &&
    entry.endsWith(BACK.getFormatting()) &&
    oreExists(entry);
 }

 /**
  * Gets a list of Item's based on a OreDict Entry
  *
  * @param entry Entry to find Items
  * @return List of Items for the OreDict Entry
  */
 private NonNullList<ItemStack> getOreItems(String entry) {
  synchronized (mainThread) {
   return OreDictionary.getOres(entry.substring(1, entry.length() - 1));
  }
 }

 /**
  * Create a list of Ore Entries based on the ItemStack
  *
  * @param stack Stack to get OreEntries
  * @return Array of OReDict Entries
  */
 private String[] getOreEntrys(ItemStack stack) {
  synchronized (mainThread) {
   int[] oreIds = OreDictionary.getOreIDs(stack);
   List<String> oreEntries = new ArrayList<>();
   for (int oreID : oreIds)
    oreEntries.add(OreDictionary.getOreName(oreID));
   return oreEntries.toArray(new String[0]);
  }
 }

 /**
  * Checks if a OreEntry exists
  *
  * @param entry Entry to check if it exists
  * @return If the entry exists (May not be loaded yet, causes issues)
  */
 private boolean oreExists(String entry) {
  synchronized (mainThread) {
   return OreDictionary.doesOreNameExist(entry.substring(1, entry.length() - 1));
  }
 }

 /**
  * Looks up a Item based on its modid:name
  *
  * @param item Item to lookup
  * @return Item from ResourceLocation
  */
 private Item getItem(ResourceLocation item) {
  synchronized (mainThread) {
   return ForgeRegistries.ITEMS.getValue(item);
  }
 }

 /**
  * Looks up a Fluid based on its name
  *
  * @param name Fluid to lookup
  * @return Fluid from String
  */
 private Fluid getFluid(String name) {
  synchronized (mainThread) {
   return FluidRegistry.getFluid(name);
  }
 }
}
