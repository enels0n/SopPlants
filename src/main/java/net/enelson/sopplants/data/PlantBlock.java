package net.enelson.sopplants.data;

import org.bukkit.Location;
import org.bukkit.Material;

public class PlantBlock {
	private Location location;
	private Material stemType;
	private Long growTime;
	
	PlantBlock(Location location, Material stemType) {
		this.location = location;
		this.stemType = stemType;
		this.growTime = System.currentTimeMillis()/1000;
	}
	
	PlantBlock(Location location, Material stemType, Long growTime) {
		this.location = location;
		this.stemType = stemType;
		this.growTime = growTime;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public Material getStem() {
		return this.stemType;
	}
	
	public Long getGrowTime() {
		return this.growTime;
	}
}
