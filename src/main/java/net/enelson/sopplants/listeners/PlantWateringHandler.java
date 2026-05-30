package net.enelson.sopplants.listeners;

import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;
import net.enelson.sopplants.utils.Utils;

public class PlantWateringHandler implements Listener {
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (e.getItem() == null
				|| (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
			return;

		String potType = Utils.getType(e.getItem());
		if (potType != null) {
			if (potType != "bottle") {
				e.setUseInteractedBlock(Result.DENY);
				e.setUseItemInHand(Result.DENY);
				e.setCancelled(true);
				if (!e.getHand().name().equals("HAND"))
					return;
			}

			int fullness = Utils.getFullness(e.getItem());
			int maxFullness = Utils.getMaxFullness(e.getItem());
			int durability = Utils.getDurability(e.getItem());

			Block block = e.getPlayer().getTargetBlock((Set<Material>) null, 4);
			if (block != null && block.getType() == Material.WATER && fullness != maxFullness) {
				if (potType == "bottle")
					return;
				Bukkit.getScheduler().runTaskLater(SopPlants.plugin, new Runnable() {
					@Override
					public void run() {
						ItemStack item = Utils.setParams(e.getItem(), -1, fullness + 1);

						if (e.getItem().getAmount() > 1) {
							e.getItem().setAmount(e.getItem().getAmount() - 1);

							if (e.getPlayer().getInventory().addItem(item).size() != 0) {
								e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
							}
							return;
						}
						e.getPlayer().getEquipment().setItem(e.getHand(), item);
					}
				}, 1);
				return;
			}

			if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || fullness == 0)
				return;

			int radius = e.getPlayer().isSneaking() ? 0 : SopPlants.config.getInt("pots." + potType + ".radius");
			int use = 0;
            World world = e.getClickedBlock().getWorld();
            int centerX = e.getClickedBlock().getX();
            int centerY = e.getClickedBlock().getY();
            int centerZ = e.getClickedBlock().getZ();
    		

			if(this.waterBlock(world, centerX, centerY, centerZ, potType))
				use++;
			else
				return;
			
			for (int x = centerX-radius; x <= centerX+radius; x++) {
				for (int z = centerZ-radius; z <= centerZ+radius; z++) {
                	
                	if(fullness - use == 0 || durability - use == 0) {
                		break;
                	}
                	
                	if(x == centerX && z == centerZ) {
                		continue;
                	}
                	
                	if(this.waterBlock(world, x, centerY, z, potType))
        				use++;
                }
            }

            int newDurability = durability - use;
            int newFullness = fullness - use;

			e.getClickedBlock().getLocation().getWorld().playSound(e.getClickedBlock().getLocation(), Sound.AMBIENT_UNDERWATER_EXIT, 0.3f, 1.0f);

			Bukkit.getScheduler().runTaskLater(SopPlants.plugin, new Runnable() {
				@Override
				public void run() {

					if(potType == "bottle") {
                        if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                            if (e.getItem().getAmount() == 1) {
                               e.getItem().setType(Material.GLASS_BOTTLE);
                            } else {
                               e.getItem().setAmount(e.getItem().getAmount() - 1);
                               ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
                               if (e.getPlayer().getInventory().addItem(new ItemStack[]{item}).size() != 0) {
                                  e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
                               }
                            }
                         }
						return;
					}
					
					ItemStack item = Utils.setParams(e.getItem(), newDurability, newFullness);
					if (item == null) {
						e.getPlayer().getLocation().getWorld().playSound(e.getPlayer().getLocation(),
								Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
					}

					if (e.getItem().getAmount() > 1) {
						e.getItem().setAmount(e.getItem().getAmount() - 1);

						if (item != null && e.getPlayer().getInventory().addItem(item).size() != 0) {
							e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
						}
						return;
					}
					e.getPlayer().getEquipment().setItem(e.getHand(), item);
				}
			}, 1);
			return;
		} else if (e.getItem().getType().equals(Material.BONE_MEAL)) {
			Block block = e.getClickedBlock();
			if (block == null)
				return;
			Watered watered = SopPlants.manager.getWatered(block.getLocation());
			if (watered == null)
				return;

			if (watered.isGrown() || (!watered.canGrow() || !watered.canFretilize())) {
				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);
			e.getItem().setAmount(e.getItem().getAmount() - 1);
			watered.addStage();
			watered.setFretilizeTime(System.currentTimeMillis() / 1000);
		}
	}
	
	private boolean waterBlock(World world, int x, int y, int z, String potType) {

        Block bl = world.getBlockAt(x, y, z);
		BlockState newState = bl.getState();
		Material blockType = newState.getType();

		switch (blockType) {
		case WHEAT:
		case BEETROOTS:
		case POTATOES:
		case CARROTS:
		case PUMPKIN_STEM:
		case MELON_STEM:
			break;
		default:
			return false;
		}
		
		Watered watered = SopPlants.manager.getWatered(bl.getLocation());
		if (watered == null) {
			SopPlants.manager.addWatered(bl.getLocation(), blockType,
					System.currentTimeMillis() / 1000);
		}
		
		
		String effectType = null;
		if (!watered.isGrown()) {
			effectType = "effect";
		} else if (watered.getType().equals(Material.PUMPKIN_STEM)
				|| watered.getType().equals(Material.MELON_STEM)) {
			effectType = "effect_on_grown";
		} else
			return false;
		
		if (!watered.isGrown() && watered.getWateringUntilTime() < System.currentTimeMillis() / 1000) {

			int min = SopPlants.config.getInt(blockType.name().toLowerCase() + ".stage_time.min");
			int max = SopPlants.config.getInt(blockType.name().toLowerCase() + ".stage_time.max");
			Long newStage = (System.currentTimeMillis() / 1000)
					+ (new Random().nextInt(max - min + 1) + min);

			watered.setNextStageTime(newStage);
		}

		Long newWateringTime = System.currentTimeMillis() / 1000
				+ SopPlants.config.getLong("pots." + potType + "." + effectType);
		
		if (newWateringTime > watered.getWateringUntilTime())
			watered.setWateringUntilTime(newWateringTime);

		bl.getWorld().spawnParticle(Particle.WATER_DROP, bl.getLocation().clone().add(0.5, 0.5, 0.5), 30);
		
		Block farmBlock =  bl.getLocation().clone().add(0,-1,0).getBlock();
		Farmland farmland = (Farmland) farmBlock.getBlockData();
		farmland.setMoisture(7);
		farmBlock.setBlockData(farmland);
		return true;
	}
}
