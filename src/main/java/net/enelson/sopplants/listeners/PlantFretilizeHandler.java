package net.enelson.sopplants.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;

public class PlantFretilizeHandler implements Listener {
	@EventHandler
	public void onFretilize(BlockFertilizeEvent e) {
		if(e.isCancelled())
			return;
		
		Block block = e.getBlock();
		switch (block.getType()) {
			case WHEAT:
			case BEETROOTS:
			case POTATOES:
			case CARROTS:
			case PUMPKIN_STEM:
			case MELON_STEM:
				e.setCancelled(true);
				return;
			default:
				return;
		}
	}
}
