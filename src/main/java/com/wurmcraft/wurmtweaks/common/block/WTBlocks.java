package com.wurmcraft.wurmtweaks.common.block;

import com.wurmcraft.wurmtweaks.common.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class WTBlocks {

	public static Block transparentAluminum;

	public static void register () {
		register (transparentAluminum = new BlockTransparentAluminum (Material.GLASS),"transparentAluminum");
	}

	private static Block register (Block block,String name) {
		Registry.registerBlock (block,name);
		return block;
	}
}