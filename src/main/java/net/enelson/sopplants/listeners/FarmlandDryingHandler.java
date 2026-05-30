package net.enelson.sopplants.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.MoistureChangeEvent;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;

public class FarmlandDryingHandler implements Listener {

	@EventHandler
	public void onDrying(BlockFromToEvent e) {
		if(e.getBlock().getType().equals(Material.FARMLAND)) {
			Watered watered = SopPlants.manager.getWatered(e.getBlock().getLocation().clone().add(0,1,0));
			if(watered != null)
				e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDrying(MoistureChangeEvent e) {
		Watered watered = SopPlants.manager.getWatered(e.getBlock().getLocation().clone().add(0,1,0));
		if(watered != null)
			e.setCancelled(true);
	}
}
