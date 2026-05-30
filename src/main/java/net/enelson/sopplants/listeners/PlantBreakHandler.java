package net.enelson.sopplants.listeners;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.PlantBlock;
import net.enelson.sopplants.data.Watered;
import net.enelson.sopplants.utils.Utils;

public class PlantBreakHandler implements Listener {
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.isCancelled())
			return;
			
		ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();
		if(item != null && Utils.getType(item) != null) {
			e.setCancelled(true);
			return;
		}

		Watered watered = SopPlants.manager.getWatered(e.getBlock().getLocation());
		if (watered != null) {
			SopPlants.manager.removeWatered(watered);
			if(!watered.isGrown())
				return;
			String type = watered.getType().name().toLowerCase();
			List<String> commands = SopPlants.config.getStringList(type+".harvestingCommands");
			if(commands != null) {
				for(String cmd : commands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", e.getPlayer().getDisplayName()).replaceAll("%plant%", type));
				}
			}
			
			for(Entry<Enchantment, Integer> entry : e.getPlayer().getEquipment().getItemInMainHand().getEnchantments().entrySet()) {
				if(entry.getKey().getKey().getKey().equalsIgnoreCase("replenish"))
					return;
			}
			Block farmBlock =  e.getBlock().getLocation().clone().add(0,-1,0).getBlock();
			farmBlock.setType(Material.DIRT);
			return;
		}

		PlantBlock plantBlock = SopPlants.manager.getPlantBlock(e.getBlock().getLocation());
		if (plantBlock != null) {
			SopPlants.manager.removePlantBlock(plantBlock);
			String stemType = plantBlock.getStem().name().toLowerCase();
			List<String> commands = SopPlants.config.getStringList(stemType+".harvestingBlockCommands");
			if(commands != null) {
				for(String cmd : commands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", e.getPlayer().getDisplayName()).replaceAll("%block%", stemType));
				}
			}
		}
	}
}
