package net.enelson.sopplants.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.utils.Utils;

public class RepairHandler implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCraft(PrepareItemCraftEvent e) {
		if(!e.getInventory().getType().equals(InventoryType.WORKBENCH) && !e.getInventory().getType().equals(InventoryType.CRAFTING))
			return;
		
		List<ItemStack> items = new ArrayList<>();
		int i = 0;
		boolean other = false;
		String type = null;
		for (ItemStack item : e.getInventory()) {
			if(i==0) {
				i++;
				continue;
			}
			if (item != null && !item.getType().equals(Material.AIR)) {
				if(items.size()>0 && (Utils.getType(item) == null || !type.equals(Utils.getType(item)))) {
					e.getInventory().setResult(null);
					return;
				}
				
				if(Utils.getType(item) == null) {
					other = true;
					continue;
				}
				
				if(other || (items.size()>0 && !type.equals(Utils.getType(item)))) {
					e.getInventory().setResult(null);
					return;
				}
				
				if(items.size()==0)
					type = Utils.getType(item);
				
				items.add(item);
				i++;
				continue;
			}
		}

		if(items.size()==0)
			return;
		
		if(items.size()==1) {
			e.getInventory().setResult(null);
			return;
		}
		
		int durability = 0;
		for(ItemStack item : items) {
			durability += Utils.getDurability(item);
		}
		
		durability = (int)(durability*1.3);
		ItemStack result = Utils.setParams(items.get(0).clone(), durability, 0);
		e.getInventory().setResult(result);
		
		Bukkit.getScheduler().runTaskLater(SopPlants.plugin, new Runnable() {
			
			@Override
			public void run() {
				e.getInventory().setResult(result);
			}
		}, 1);
	}
	
	@EventHandler
	public void onCraft1(CraftItemEvent e) {
		if(!e.getInventory().getType().equals(InventoryType.WORKBENCH) && !e.getInventory().getType().equals(InventoryType.CRAFTING))
			return;
		
		List<ItemStack> items = new ArrayList<>();
		int i = 0;
		boolean other = false;
		String type = null;
		for (ItemStack item : e.getInventory()) {
			if(i==0) {
				i++;
				continue;
			}
			if (item != null && !item.getType().equals(Material.AIR)) {
				if(items.size()>0 && (Utils.getType(item) == null || !type.equals(Utils.getType(item)))) {
					e.getInventory().setResult(null);
					return;
				}
				
				if(Utils.getType(item) == null) {
					other = true;
					continue;
				}
				
				if(other || (items.size()>0 && !type.equals(Utils.getType(item)))) {
					e.getInventory().setResult(null);
					return;
				}
				
				if(items.size()==0)
					type = Utils.getType(item);
				
				items.add(item);
				i++;
				continue;
			}
		}

		if(items.size()==0)
			return;
		
		if(items.size()==1) {
			e.getInventory().setResult(null);
			return;
		}
		
		int durability = 0;
		for(ItemStack item : items) {
			durability += Utils.getDurability(item);
		}
		
		durability = (int)(durability*1.3);
		ItemStack result = Utils.setParams(items.get(0).clone(), durability, 0);
		e.getInventory().setResult(result);
	}
}
