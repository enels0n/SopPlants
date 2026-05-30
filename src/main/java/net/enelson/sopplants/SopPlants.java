package net.enelson.sopplants;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.ClassPath;

import net.enelson.sopplants.commands.MainCommand;
import net.enelson.sopplants.data.WateringManager;

public class SopPlants extends JavaPlugin {
	public static Plugin plugin;
	public static WateringManager manager;
	public static File fileConfig;
	public static YamlConfiguration config;
	
	public void onEnable() {
		plugin = this;

		SopPlants.fileConfig = new File(getDataFolder(), "config.yml");
		if (!SopPlants.fileConfig.exists()) saveResource("config.yml", true);
		SopPlants.config = YamlConfiguration.loadConfiguration(SopPlants.fileConfig);
		
		manager = new WateringManager();
		
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			String pac = "net.enelson.sopplants.listeners";
			for (ClassPath.ClassInfo clazzInfo : ClassPath.from(getClassLoader()).getTopLevelClasses(pac)) {
				Class<?> clazz = Class.forName(clazzInfo.getName());
				if (Listener.class.isAssignableFrom(clazz)) {
					pluginManager.registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.getCommand("sopplants").setExecutor(new MainCommand());
	}

	
	public void onDisable() {
		manager.onDisable();
	}
}

