package net.enelson.sopplants.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.event.PlantActionEvent;

public class PlantPlaceHandler implements Listener {
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		
		Block block = e.getBlock();
		switch (block.getType()) {
			case WHEAT:
			case BEETROOTS:
			case POTATOES:
			case CARROTS:
			case PUMPKIN_STEM:
			case MELON_STEM:
				SopPlants.manager.addWatered(block.getLocation(), block.getType(), System.currentTimeMillis() / 1000);
				Bukkit.getPluginManager().callEvent(new PlantActionEvent(e.getPlayer(), "PLANT", block.getType().name().toLowerCase()));
				break;
			default:
				return;
		}
	}
}
