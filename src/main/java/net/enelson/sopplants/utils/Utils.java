package net.enelson.sopplants.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import net.enelson.sopli.lib.text.TextUtils;
import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;
import net.minecraft.nbt.NBTTagCompound;

public class Utils {
	private static final TextUtils TEXT_UTILS = new TextUtils();

	public static Location getDeserializedLocation(String s) {
		final String[] split = s.split(",");
		return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
				Double.parseDouble(split[3]));
	}

	public static String getSerializedLocation(Location loc) {
		return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
	}

	public static CropState getNextStage(CropState state) {
		switch (state) {
			case SEEDED:
			case GERMINATED:
				return CropState.VERY_SMALL;
			case VERY_SMALL:
				return CropState.SMALL;
			case SMALL:
				return CropState.MEDIUM;
			case MEDIUM:
				return CropState.TALL;
			case TALL:
				return CropState.VERY_TALL;
			default:
				return CropState.RIPE;
		}
	}
	
	public static ItemStack createPot(String type) {
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(new ItemStack(Material.SHEARS));
		NBTTagCompound tag = stack.w();
		int durability = SopPlants.config.getInt("pots."+type+".durability");
		int fullness = SopPlants.config.getInt("pots."+type+".fullness");
		tag.a("SopPlants-type", type);
		tag.a("SopPlants-fullness", 0);
		tag.a("SopPlants-durability", durability);
		tag.a("SopPlants-maxfullness", fullness);
		tag.a("SopPlants-maxdurability", durability);
		stack.c(tag);
		ItemStack item = CraftItemStack.asBukkitCopy(stack);
		item.setAmount(1);
		ItemMeta meta  = item.getItemMeta();
		meta.setDisplayName(TEXT_UTILS.color(SopPlants.config.getString("locale."+type+"_name")));
		List<String> lore = new ArrayList<>();
		lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.fullness") + ": " + 0 +"/" + fullness));
		lore.add("");
		lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.durability") + ": " + durability +"/" + durability));
		meta.setLore(lore);
		meta.setCustomModelData(SopPlants.config.getInt("pots."+type+".model"));
		item.setItemMeta(meta);
		return item;
	}
	
	public static int getDurability(ItemStack item) {
		if(item.getType().equals(Material.POTION) && (((PotionMeta)item.getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER)))
			return 1;
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = stack.w();

		if (tag == null)
			return 0;

		if (tag.e("SopPlants-durability")) {
			return tag.h("SopPlants-durability");
		}

		return 0;
	}
	
	public static int getFullness(ItemStack item) {
		if(item.getType().equals(Material.POTION) && (((PotionMeta)item.getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER)))
			return 1;
		
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = stack.w();

		if (tag == null)
			return 0;

		if (tag.e("SopPlants-fullness")) {
			return tag.h("SopPlants-fullness");
		}

		return 0;
	}
	
	public static int getMaxFullness(ItemStack item) {
		if(item.getType().equals(Material.POTION) && (((PotionMeta)item.getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER)))
			return 1;
		
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = stack.w();

		if (tag == null)
			return 0;

		if (tag.e("SopPlants-maxfullness")) {
			return tag.h("SopPlants-maxfullness");
		}

		return 0;
	}
	
	public static String getType(ItemStack item) {
		if(item.getType().equals(Material.POTION) && (((PotionMeta)item.getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER)))
			return "bottle";
		
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = stack.w();

		if (tag == null)
			return null;

		if (tag.e("SopPlants-type")) {
			return tag.l("SopPlants-type");
		}

		return null;
	}
	
	public static ItemStack setParams(ItemStack item, int durability, int fullness) {
		
		net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = stack.w();
		
		if(durability == 0)
			return null;
		if(durability > 0)
			tag.a("SopPlants-durability", durability);
		if(fullness >= 0)
			tag.a("SopPlants-fullness", fullness);
		durability = tag.h("SopPlants-durability");
		
		stack.c(tag);
		item = CraftItemStack.asBukkitCopy(stack);


		ItemMeta meta  = item.getItemMeta();
		String type = tag.l("SopPlants-type");
		List<String> lore = new ArrayList<>();
		int maxDurability = tag.h("SopPlants-maxdurability");
		if(durability > maxDurability)
			durability = maxDurability;
		lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.fullness") + ": " + fullness +"/" + tag.h("SopPlants-maxfullness")));
		lore.add("");
		lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.durability") + ": " + durability +"/" + maxDurability));

		meta.setLore(lore);
		
		int model = (int)Math.ceil((double)fullness/tag.h("SopPlants-maxfullness")*5);
		meta.setCustomModelData(SopPlants.config.getInt("pots."+type+".model")+model);
		int itemMaxDurability = item.getType().getMaxDurability();
		int itemDurability = Math.max(0, itemMaxDurability - (int)((((double)durability) / maxDurability) * itemMaxDurability));
		((Damageable)meta).setDamage(itemDurability);
		item.setItemMeta(meta);
		item.setAmount(1);
		return item;
	}
	

	public static void setFarmlandStatus(Watered watered, int status) {
		Block farmBlock = watered.getLocation().clone().add(0, -1, 0).getBlock();
		if (!(farmBlock.getBlockData() instanceof Farmland))
			return;
		Farmland farmland = (Farmland) farmBlock.getBlockData();
		if (status < 0)
			farmBlock.setType(Material.DIRT);
		else
			farmland.setMoisture(status);
		farmBlock.setBlockData(farmland);
	}
}
