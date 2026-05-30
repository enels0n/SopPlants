package net.enelson.sopplants.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopplants.utils.Utils;

public class MainCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.isOp())
			return false;

		if(args.length == 0) { 
			sender.sendMessage("/sopplants give <player> <type>");
			return false;
		}
		
		// /sopplants give <player> <type>
		if(args[0].equals("give")) { 
			if(args.length != 3) {
				sender.sendMessage("/sopplants give <player> <type>");
				return false;
			}
			
			Player player = Bukkit.getPlayerExact(args[1]);
			if(player == null) {
				sender.sendMessage("Игрок не найден");
				return false;
			}
			
			ItemStack item = Utils.createPot(args[2]);
			if(player.getInventory().addItem(item).size() != 0) {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			return false;
		}
		return false;
	}

}
