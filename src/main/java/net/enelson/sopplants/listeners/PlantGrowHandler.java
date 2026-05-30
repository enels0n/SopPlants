package net.enelson.sopplants.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;

public class PlantGrowHandler implements Listener {

	@EventHandler
	public void onGrow(BlockGrowEvent e) {
		if (e.isCancelled())
			return;
		
		Material type = e.getNewState().getType();
		switch (type) {
		case WHEAT:
		case BEETROOTS:
		case POTATOES:
		case CARROTS:
		case PUMPKIN_STEM:
		case MELON_STEM:
			break;
		default:
			return;
		}

		e.setCancelled(true);
		
		Watered watered = SopPlants.manager.getWatered(e.getBlock().getLocation());
		if (watered == null) {
			SopPlants.manager.addWatered(e.getBlock().getLocation(), type, System.currentTimeMillis()/1000);
		}
	}
}
