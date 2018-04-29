package com.wurmcraft.wurmtweaks.script;

import com.wurmcraft.wurmtweaks.common.ConfigHandler;
import com.wurmcraft.wurmtweaks.script.thread.WorkerThread;
import com.wurmcraft.wurmtweaks.utils.LogHandler;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptDownloader {

	public String masterScript;
	public File saveLocation;
	public List <String> slaveScripts = new ArrayList <> ();
	private String slaveScript;
	private WurmScript wurmScript;

	public ScriptDownloader (String mainScript,File saveLocation,String slaveScript) {
		this.masterScript = mainScript;
		this.saveLocation = saveLocation;
		this.slaveScript = slaveScript;
		init ();
	}

	public void init () {
		if (ConfigHandler.checkForRecipeUpdates)
			downloadFile (masterScript,"master.ws");
		if (new File (saveLocation + File.separator + "master.ws").exists ()) {
			try {
				List <String> masterScriptLines = Files.readAllLines (new File (saveLocation + File.separator + "master.ws").toPath ());
				String[] withCommentsRemoved = WurmScript.removeComments (masterScriptLines.toArray (new String[0]));
				Collections.addAll (slaveScripts,withCommentsRemoved);
				if (wurmScript == null)
					wurmScript = new WurmScript ();
				wurmScript.init ();
				downloadSlaveScripts ();
				processSlaveScripts ();
				if (WurmScript.reload) {
					ForgeRegistry <IRecipe> recipeRegistry = (ForgeRegistry <IRecipe>) ForgeRegistries.RECIPES;
					recipeRegistry.freeze ();
					WurmScript.reload = false;
				}
			} catch (IOException e) {
				LogHandler.info ("Unable To Load 'master.ws' from disk");
			}
		} else
			LogHandler.info ("Failed to find master.ws");
	}

	public void reload () {
		wurmScript.reload ();
		init ();
	}

	private void downloadFile (String loc,String fileName) {
		try {
			URL url = new URL (loc);
			File location = new File (saveLocation + File.separator + fileName);
			if (ConfigHandler.checkForRecipeUpdates || !location.exists ())
				try {
					FileUtils.copyURLToFile (url,location);
				} catch (IOException e) {
					LogHandler.info ("Unable to " + (location.exists () ? "update" : "download / save") + ", " + loc + " I/O exception");
				}
		} catch (MalformedURLException e) {
			LogHandler.info (loc + " is not a valid URL! (Unable To Activate WurmScript)");
		}
	}

	private void downloadSlaveScripts () {
		for (String script : slaveScripts)
			downloadFile (slaveScript + "/" + script,script);
	}

	private void processSlaveScripts () {
		if (slaveScript.length () > 0)
			if (ConfigHandler.multithread) {
				String[][] threadWork = new String[][] {slaveScripts.subList (0,slaveScripts.size () / 2).toArray (new String[0]),slaveScripts.subList ((slaveScripts.size () / 2) + 1,slaveScripts.size ()).toArray (new String[0])};
				for (int index = 0; index < threadWork.length; index++) {
					WorkerThread workerThread = new WorkerThread (index + 1,convertToFiles (threadWork[index]));
					workerThread.start ();
				}
			} else {
				for (String script : slaveScripts)
					wurmScript.process (new File (saveLocation + File.separator + script));
			}
	}

	private File[] convertToFiles (String[] scriptNames) {
		List <File> files = new ArrayList <> ();
		for (String name : scriptNames)
			files.add (new File (saveLocation + File.separator + name));
		return files.toArray (new File[0]);
	}
}