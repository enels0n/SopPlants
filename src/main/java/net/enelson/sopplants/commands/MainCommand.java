package net.enelson.sopplants.commands;

import net.enelson.sopli.lib.text.TextUtils;
import net.enelson.sopplants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private static final TextUtils TEXT_UTILS = new TextUtils();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("give")) {
            sendUsage(sender);
            return true;
        }

        if (args.length != 3) {
            sendUsage(sender);
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(TEXT_UTILS.color("&cИгрок не найден."));
            return true;
        }

        String type = args[2].toLowerCase();
        if (!Utils.isGiveableType(type)) {
            sender.sendMessage(TEXT_UTILS.color("&cНеизвестный тип лейки: &e" + args[2]));
            sender.sendMessage(TEXT_UTILS.color("&7Доступные: &f" + String.join(", ", Utils.getGiveableTypes())));
            return true;
        }

        ItemStack item = Utils.createPot(type);
        if (item == null) {
            sender.sendMessage(TEXT_UTILS.color("&cНе удалось создать лейку: &e" + type));
            return true;
        }

        if (!player.getInventory().addItem(item).isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }

        sender.sendMessage(TEXT_UTILS.color("&aВыдана лейка &e" + type + " &aигроку &e" + player.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(Collections.singletonList("give"), args[0]);
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                List<String> names = new ArrayList<String>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    names.add(online.getName());
                }
                return filter(names, args[1]);
            }
            if (args.length == 3) {
                return filter(Utils.getGiveableTypes(), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(TEXT_UTILS.color("&e/sopplants give <player> <type>"));
        sender.sendMessage(TEXT_UTILS.color("&7Типы: &f" + String.join(", ", Utils.getGiveableTypes())));
    }

    private List<String> filter(List<String> options, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return options;
        }
        String lower = prefix.toLowerCase();
        List<String> result = new ArrayList<String>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
