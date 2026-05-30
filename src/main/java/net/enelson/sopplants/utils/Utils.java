package net.enelson.sopplants.utils;

import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.external.ItemNBTUtils;
import net.enelson.sopli.lib.item.ItemUtils;
import net.enelson.sopli.lib.text.TextUtils;
import net.enelson.sopplants.SopPlants;
import net.enelson.sopplants.data.Watered;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static final TextUtils TEXT_UTILS = new TextUtils();
    private static final String KEY_TYPE = "SopPlants-type";
    private static final String KEY_FULLNESS = "SopPlants-fullness";
    private static final String KEY_DURABILITY = "SopPlants-durability";
    private static final String KEY_MAX_FULLNESS = "SopPlants-maxfullness";
    private static final String KEY_MAX_DURABILITY = "SopPlants-maxdurability";

    private static ItemUtils itemUtils() {
        return SopLib.getInstance().getItemUtils();
    }

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
        int durability = SopPlants.config.getInt("pots." + type + ".durability");
        int fullness = SopPlants.config.getInt("pots." + type + ".fullness");
        int model = SopPlants.config.getInt("pots." + type + ".model");

        List<String> lore = new ArrayList<String>();
        lore.add(SopPlants.config.getString("locale.fullness") + ": 0/" + fullness);
        lore.add("");
        lore.add(SopPlants.config.getString("locale.durability") + ": " + durability + "/" + durability);

        return itemUtils().createItem(
                "SHEARS",
                1,
                model,
                SopPlants.config.getString("locale." + type + "_name"),
                null,
                lore,
                Arrays.asList(
                        KEY_TYPE + "::" + type,
                        KEY_FULLNESS + "::0",
                        KEY_DURABILITY + "::" + durability,
                        KEY_MAX_FULLNESS + "::" + fullness,
                        KEY_MAX_DURABILITY + "::" + durability
                )
        );
    }

    public static int getDurability(ItemStack item) {
        if (isWaterBottle(item)) {
            return 1;
        }
        return parseIntTag(item, KEY_DURABILITY, 0);
    }

    public static int getFullness(ItemStack item) {
        if (isWaterBottle(item)) {
            return 1;
        }
        return parseIntTag(item, KEY_FULLNESS, 0);
    }

    public static int getMaxFullness(ItemStack item) {
        if (isWaterBottle(item)) {
            return 1;
        }
        return parseIntTag(item, KEY_MAX_FULLNESS, 0);
    }

    public static String getType(ItemStack item) {
        if (isWaterBottle(item)) {
            return "bottle";
        }
        return itemUtils().getNBT(item, KEY_TYPE, String.class);
    }

    public static ItemStack setParams(ItemStack item, int durability, int fullness) {
        if (item == null) {
            return null;
        }
        if (durability == 0) {
            return null;
        }

        ItemStack updated = item.clone();
        updated.setAmount(1);

        String type = getType(updated);
        if (type == null || "bottle".equals(type)) {
            return updated;
        }

        int currentDurability = parseIntTag(updated, KEY_DURABILITY, 0);
        int currentFullness = parseIntTag(updated, KEY_FULLNESS, 0);
        int maxFullness = parseIntTag(updated, KEY_MAX_FULLNESS, 0);
        int maxDurability = parseIntTag(updated, KEY_MAX_DURABILITY, 0);

        if (durability > 0) {
            currentDurability = durability;
        }
        if (fullness >= 0) {
            currentFullness = fullness;
        }
        if (currentDurability > maxDurability) {
            currentDurability = maxDurability;
        }

        ItemNBTUtils.setTags(updated, Arrays.asList(
                KEY_TYPE + "::" + type,
                KEY_FULLNESS + "::" + currentFullness,
                KEY_DURABILITY + "::" + currentDurability,
                KEY_MAX_FULLNESS + "::" + maxFullness,
                KEY_MAX_DURABILITY + "::" + maxDurability
        ));

        ItemMeta meta = updated.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.fullness") + ": " + currentFullness + "/" + maxFullness));
        lore.add("");
        lore.add(TEXT_UTILS.color(SopPlants.config.getString("locale.durability") + ": " + currentDurability + "/" + maxDurability));
        meta.setLore(lore);

        int model = (int) Math.ceil((double) currentFullness / maxFullness * 5);
        meta.setCustomModelData(SopPlants.config.getInt("pots." + type + ".model") + model);
        int itemMaxDurability = updated.getType().getMaxDurability();
        int itemDurability = Math.max(0,
                itemMaxDurability - (int) ((((double) currentDurability) / maxDurability) * itemMaxDurability));
        ((Damageable) meta).setDamage(itemDurability);
        updated.setItemMeta(meta);
        return updated;
    }

    public static void setFarmlandStatus(Watered watered, int status) {
        Block farmBlock = watered.getLocation().clone().add(0, -1, 0).getBlock();
        if (!(farmBlock.getBlockData() instanceof Farmland)) {
            return;
        }
        Farmland farmland = (Farmland) farmBlock.getBlockData();
        if (status < 0) {
            farmBlock.setType(Material.DIRT);
        } else {
            farmland.setMoisture(status);
        }
        farmBlock.setBlockData(farmland);
    }

    private static boolean isWaterBottle(ItemStack item) {
        return item != null
                && item.getType().equals(Material.POTION)
                && item.getItemMeta() instanceof PotionMeta
                && ((PotionMeta) item.getItemMeta()).getBasePotionData().getType().equals(PotionType.WATER);
    }

    private static int parseIntTag(ItemStack item, String key, int fallback) {
        String raw = itemUtils().getNBT(item, key, String.class);
        if (raw == null || raw.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
