package net.enelson.sopplants.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.utils.Utils;

public class WateringManager {

	private List<Watered> watered;
	private List<PlantBlock> plantBlocks;
	private BukkitTask tasker;
	private BukkitTask taskerBlocks;
	private BukkitTask saver;
	private File fileWatered;
	private YamlConfiguration configWatered;

	public WateringManager() {
		this.watered = new ArrayList<Watered>();
		this.plantBlocks = new ArrayList<PlantBlock>();

		this.fileWatered = new File(SopPlants.plugin.getDataFolder(), "watered.yml");
		if (!this.fileWatered.exists())
			SopPlants.plugin.saveResource("watered.yml", true);
		this.configWatered = YamlConfiguration.loadConfiguration(this.fileWatered);

		if (this.configWatered.getConfigurationSection("plants") != null) {
			for (String id : this.configWatered.getConfigurationSection("plants").getKeys(false)) {
				this.addWatered(
						Utils.getDeserializedLocation(this.configWatered.getString("plants." + id + ".location")),
						Material.valueOf(this.configWatered.getString("plants." + id + ".type")),
						this.configWatered.getLong("plants." + id + ".wateringTime"),
						this.configWatered.getBoolean("plants." + id + ".grown"),
						this.configWatered.getBoolean("plants." + id + ".watered"));
			}
		}

		if (this.configWatered.getConfigurationSection("blocks") != null) {
			for (String id : this.configWatered.getConfigurationSection("blocks").getKeys(false)) {
				this.addPlantBlock(
						Utils.getDeserializedLocation(this.configWatered.getString("blocks." + id + ".location")),
						Material.valueOf(this.configWatered.getString("blocks." + id + ".stemType")),
						this.configWatered.getLong("blocks." + id + ".growTime"));
			}
		}

		this.tasker = Bukkit.getScheduler().runTaskTimer(SopPlants.plugin, new Runnable() {
			@Override
			public void run() {
				Iterator<Watered> iterator = watered.iterator();
				while (iterator.hasNext()) {
					Watered watered = iterator.next();

					int dryTime = 0;
					if (!watered.getLocation().getBlock().getType().equals(watered.getType())) {
						iterator.remove();
						continue;
					}

					if (!watered.isGrown()) {
						if (watered.getLocation().getWorld().hasStorm()) {
							checkStorm(watered);
						}
						
						if (watered.getWateringUntilTime() - SopPlants.config.getInt("farmlandDryTime") < System.currentTimeMillis() / 1000) {
							Utils.setFarmlandStatus(watered, 0);
						}
						
						dryTime = SopPlants.config.getInt(watered.getType().name().toLowerCase() + ".dry");
						if (watered.getWateringUntilTime() + dryTime < System.currentTimeMillis() / 1000) {
							watered.getLocation().getBlock().setType(Material.AIR);
							watered.getLocation().getWorld().playSound(watered.getLocation(),
									Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 3.0f);
							iterator.remove();
							Utils.setFarmlandStatus(watered, -1);
							continue;
						}

						if (watered.canGrow() && watered.getNextStage() <= System.currentTimeMillis() / 1000
								&& watered.getWateringUntilTime() >= System.currentTimeMillis() / 1000) {
							watered.addStage();
						}
						
					} else {
						if (watered.getType().equals(Material.PUMPKIN_STEM)
								|| watered.getType().equals(Material.MELON_STEM)) {
							if (watered.getLocation().getWorld().hasStorm()) {
								checkStorm(watered);
							}

							if (watered.getWateringUntilTime() - SopPlants.config.getInt("farmlandDryTime") < System.currentTimeMillis() / 1000) {
								Utils.setFarmlandStatus(watered, 0);
							}
							
							dryTime = SopPlants.config.getInt(watered.getType().name().toLowerCase() + ".dry_on_grown");
						} else {
							dryTime = SopPlants.config.getInt(watered.getType().name().toLowerCase() + ".rot");
						}
						if (watered.getWateringUntilTime() + dryTime < System.currentTimeMillis() / 1000) {
							watered.getLocation().getBlock().setType(Material.AIR);
							watered.getLocation().getWorld().playSound(watered.getLocation(),
									Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 3.0f);
							if (new Random().nextInt(4) == 0)
								watered.getLocation().getWorld().dropItem(watered.getLocation(),
										new ItemStack(Material.BONE_MEAL, 1));
							iterator.remove();
							Utils.setFarmlandStatus(watered, -1);
						}
					}

				}

			}
		}, 20, 20);

		this.taskerBlocks = Bukkit.getScheduler().runTaskTimer(SopPlants.plugin, new Runnable() {
			@Override
			public void run() {
				Iterator<PlantBlock> iterator = plantBlocks.iterator();
				while (iterator.hasNext()) {
					PlantBlock plantBlock = iterator.next();
					int time = SopPlants.config.getInt(plantBlock.getStem().name().toLowerCase() + ".rot_block");
					if (plantBlock.getGrowTime() + time < System.currentTimeMillis() / 1000) {
						plantBlock.getLocation().getBlock().setType(Material.AIR);
						if (new Random().nextInt(4) == 0)
							plantBlock.getLocation().getWorld().dropItem(plantBlock.getLocation(),
									new ItemStack(Material.BONE_MEAL, 1));
						plantBlock.getLocation().getWorld().playSound(plantBlock.getLocation(),
								Sound.BLOCK_HONEY_BLOCK_BREAK, 1.0f, 3.0f);
						iterator.remove();
					}
				}
			}

		}, 20, 20);

		this.saver = Bukkit.getScheduler().runTaskTimer(SopPlants.plugin, new Runnable() {
			@Override
			public void run() {
				saveAll();
			}

		}, 6000, 6000);
	}

	public void addWatered(Location location, Material type, Long time) {
		this.addWatered(location, type, time, false, false);
	}

	public void addWatered(Location location, Material type, Long time, boolean grown, boolean watered) {
		int min = SopPlants.config.getInt(type.name().toLowerCase() + ".stage_time.min");
		int max = SopPlants.config.getInt(type.name().toLowerCase() + ".stage_time.max");
		int rand = new Random().nextInt(max - min + 1) + min;
		Long nextStage = (System.currentTimeMillis() / 1000) + rand;
		this.watered.add(new Watered(location, type, time, nextStage, grown, watered));
	}

	public void removeWatered(Watered watered) {
		this.watered.remove(watered);
	}

	public void removeWatered(Location location) {
		this.removeWatered(getWatered(location));
	}

	public Watered getWatered(Location location) {
		return this.watered.stream().filter(w -> w.getLocation().equals(location)).findFirst().orElse(null);
	}

	public void addPlantBlock(Location location, Material stemType) {
		this.plantBlocks.add(new PlantBlock(location, stemType));
	}

	public void addPlantBlock(Location location, Material stemType, Long growTime) {
		this.plantBlocks.add(new PlantBlock(location, stemType, growTime));
	}

	public void removePlantBlock(PlantBlock plantBlock) {
		this.plantBlocks.remove(plantBlock);
	}

	public void removePlantBlock(Location location) {
		this.removeWatered(getWatered(location));
	}

	public PlantBlock getPlantBlock(Location location) {
		return this.plantBlocks.stream().filter(w -> w.getLocation().equals(location)).findFirst().orElse(null);
	}

	public void saveAll() {
		this.configWatered.set("plants", null);
		this.configWatered.set("blocks", null);
		this.watered.stream().forEach(w -> {
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".location",
					Utils.getSerializedLocation(w.getLocation()));
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".type", w.getType().name());
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".wateringTime",
					w.getWateringUntilTime());
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".grown", w.isGrown());
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".nextStage", w.getNextStage());
			this.configWatered.set("plants." + this.generateId(w.getLocation()) + ".watered", w.isWatered());
		});
		this.plantBlocks.stream().forEach(p -> {
			this.configWatered.set("blocks." + this.generateId(p.getLocation()) + ".location",
					Utils.getSerializedLocation(p.getLocation()));
			this.configWatered.set("blocks." + this.generateId(p.getLocation()) + ".stemType", p.getStem().name());
			this.configWatered.set("blocks." + this.generateId(p.getLocation()) + ".growTime", p.getGrowTime());
		});

		try {
			this.configWatered.save(fileWatered);
		} catch (IOException e) {
		}
	}

	public void onDisable() {
		this.saveAll();
		this.saver.cancel();
		this.tasker.cancel();
		this.taskerBlocks.cancel();
	}

	private String generateId(Location location) {
		String s = location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_"
				+ location.getBlockZ();
		return s;
	}
	
	private void checkStorm(Watered watered) {
		if (watered.getLocation().getBlock().getTemperature() > 0.15
				&& watered.getLocation().getBlock().getTemperature() <= 0.95) {
			if (watered.getLocation().getWorld().getHighestBlockYAt(
					watered.getLocation()) == watered.getLocation().getBlockY() - 1) {
				if (!watered.isWatered()) {
					watered.setWatered();
				}
				String effect = watered.isGrown() ? "effect_on_grown" : "effect";
				Long newWateringTime = System.currentTimeMillis() / 1000
						+ SopPlants.config.getLong("pots.rain." + effect);
				if (newWateringTime > watered.getWateringUntilTime()) {
					watered.setWateringUntilTime(newWateringTime);
					Utils.setFarmlandStatus(watered, 7);
				}
			}
		}
	}
}
