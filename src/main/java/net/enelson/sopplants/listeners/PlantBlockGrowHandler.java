package net.enelson.sopplants.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;

public class PlantBlockGrowHandler implements Listener {
	@EventHandler
	public void onGrowBlock(BlockPhysicsEvent e) {
		if (e.isCancelled())
			return;
		
		int effect = 0;
		Material type;
		if (e.getSourceBlock().getType() == Material.ATTACHED_MELON_STEM
				&& e.getBlock().getType() == Material.MELON) {
			effect = SopPlants.config.getInt("melon_stem.effect_on_grown");
			type = Material.MELON_STEM;
		} else if (e.getSourceBlock().getType() == Material.ATTACHED_PUMPKIN_STEM
				&& e.getBlock().getType() == Material.PUMPKIN) {
			effect = SopPlants.config.getInt("pumpkin_stem.effect_on_grown");
			type = Material.PUMPKIN_STEM;
		} else
			return;

		Watered watered = SopPlants.manager.getWatered(e.getSourceBlock().getLocation());

		if (watered != null) {
			if (watered.getWateringUntilTime() + effect < System.currentTimeMillis() / 1000) {
				e.setCancelled(true);
				e.getBlock().setType(Material.AIR);
			} else {
				SopPlants.manager.addPlantBlock(e.getBlock().getLocation(), type);
			}
			return;
		}
		else {
			e.setCancelled(true);
			e.getSourceBlock().setType(Material.AIR);
		}
	}
}
