package net.enelson.sopplants.data;

import java.util.Random;

import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;

import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.utils.Utils;

@SuppressWarnings("deprecation")
public class Watered {
	private Location location;
	private Material type;
	private Long wateringUntilTime;
	private boolean grown;
	private Long nextStageTime;
	private boolean watered;
	private Long fretilizeTime;
	
	Watered(Location location, Material type, Long wateringUntilTime, Long nextStage, boolean grown, boolean watered) {
		this.location = location;
		this.type = type;
		this.wateringUntilTime = wateringUntilTime;
		this.nextStageTime = nextStage;
		this.grown = grown;
		this.watered = watered;
		this.fretilizeTime = 0l;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public Material getType() {
		return this.type;
	}
	
	public Long getWateringUntilTime() {
		return this.wateringUntilTime;
	}
	
	public Long getNextStage() {
		return this.nextStageTime;
	}
	
	public Long getFretilizeTime() {
		return this.fretilizeTime;
	}
	
	public void setWateringUntilTime(Long wateringUntilTime) {
		this.wateringUntilTime = wateringUntilTime;
	}
	
	public void setFretilizeTime(Long fretilizeTime) {
		this.fretilizeTime = fretilizeTime;
	}
	
	public void setNextStageTime(Long nextStageTime) {
		this.nextStageTime = nextStageTime;
	}
	
	public void setWatered() {
		this.watered = true;
	}
	
	public boolean isGrown() {
		return this.grown;
	}
	
	public boolean isWatered() {
		return this.watered;
	}
	
	public boolean canGrow() {
		return (this.wateringUntilTime + SopPlants.config.getInt(this.type.name().toLowerCase() + ".effect") > System
				.currentTimeMillis() / 1000);
	}
	
	public boolean canFretilize() {
		return (this.fretilizeTime + SopPlants.config.getInt(this.type.name().toLowerCase() + ".bone_meal_cooldown") < System
				.currentTimeMillis() / 1000);
	}
	
	public void setGrown() {
		this.grown = true;
		this.wateringUntilTime = System.currentTimeMillis()/1000;
	}
	
	public void addStage() {
		Block block = this.location.getBlock();
		BlockState state = block.getState();
		MaterialData data = state.getData();

		if (data instanceof Crops) {
			Crops crops = (Crops) data;
			CropState newGrowLevel = Utils.getNextStage(crops.getState());
			crops.setState(newGrowLevel);
			state.setData(crops);
			block.setBlockData(state.getBlockData());
			if (crops.getState().equals(CropState.RIPE)) {
				this.setGrown();
				Utils.setFarmlandStatus(this, 0);
				return;
			}
		} else if (data instanceof MaterialData) {
			MaterialData crops = (MaterialData) data;
			int newGrowLevel = crops.getData() + 1;
			if (newGrowLevel >= 8) {
				this.setGrown();
				crops.setData((byte) 8);
				return;
			}
			crops.setData((byte) newGrowLevel);
			state.setData(crops);
			block.setBlockData(state.getBlockData());
		}

		int min = SopPlants.config.getInt(this.type.name().toLowerCase() + ".stage_time.min");
		int max = SopPlants.config.getInt(this.type.name().toLowerCase() + ".stage_time.max");
		Long newStage = (System.currentTimeMillis() / 1000)
				+ (new Random().nextInt(max - min + 1) + min);
		this.setNextStageTime(newStage);
	}
}
