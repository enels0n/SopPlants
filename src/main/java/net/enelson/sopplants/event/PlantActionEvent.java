package net.enelson.sopplants.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlantActionEvent extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String action;
	private final String plantId;

	public PlantActionEvent(Player player, String action, String plantId) {
		this.player = player;
		this.action = action;
		this.plantId = plantId;
	}

	public Player getPlayer() {
		return this.player;
	}

	public String getAction() {
		return this.action;
	}

	public String getPlantId() {
		return this.plantId;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
