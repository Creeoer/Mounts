package creeoer.plugins.mounts.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.HorseMountBuilder;

public class MountLoader {
    private YamlConfiguration horseFile;
    private Mounts main;

    /** Serealizes all the mounts into a set from the config. */

    private HashMap<HorseMount, ItemStack> horseMountAndSaddleMap;

    public MountLoader(Mounts pluginInstance) {
	main = pluginInstance;
	horseFile = (YamlConfiguration) pluginInstance.getConfig();

	loadHorseMountsAndCreateSaddles();
    }

    public HashMap<HorseMount, ItemStack> getHorseMountAndSaddleMap() {
	return horseMountAndSaddleMap;
    }

    protected void loadHorseMountsAndCreateSaddles() {
	horseMountAndSaddleMap = new LinkedHashMap<>();
	horseMountAndSaddleMap.clear();

	for (String horseSection : horseFile.getConfigurationSection("Horse Rents").getKeys(false)) {
	    String name = horseFile.getString("Horse Rents." + horseSection + ".name");
	    int speed = horseFile.getInt("Horse Rents." + horseSection + ".speed");
	    double price = horseFile.getDouble("Horse Rents." + horseSection + ".price");

	    long time = horseFile.getLong("Horse Rents." + horseSection + ".time");
	    boolean isOwnable = horseFile.getBoolean("Horse Rents." + horseSection + ".isOwnable");
	    Horse.Color color = HorseUtils
		    .parseColorFromString(horseFile.getString("Horse Rents." + horseSection + ".color"));
	    Material armorMaterial = HorseUtils
		    .parseArmorFromString(horseFile.getString("Horse Rents." + horseSection + ".armor"));

	    ItemStack armor = new ItemStack(armorMaterial, 1);

	    HorseMountBuilder builder = new HorseMountBuilder(name, speed, horseSection);
	    HorseMount mount = builder.setRentTime(time).setArmorType(armor).setOwnable(isOwnable).setPrice(price)
		    .setColor(color).build();

	    horseMountAndSaddleMap.put(mount, createHorseSaddle(mount));
	}
    }

    private ItemStack createHorseSaddle(HorseMount mount) {
	ItemStack saddle = new ItemStack(mount.getArmor().getType(), 1);

	if (mount.getArmor().getType() == Material.AIR) {
	    saddle.setType(Material.SADDLE);
	}

	ItemMeta meta = saddle.getItemMeta();

	List<String> lore = new ArrayList<>();

	meta.setDisplayName(mount.getName());
	lore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----" + ChatColor.RESET + ChatColor.DARK_GRAY
		+ "«" + ChatColor.AQUA + " Stat " + ChatColor.DARK_GRAY + "»" + ChatColor.DARK_GRAY
		+ ChatColor.STRIKETHROUGH + "----");
	lore.add(ChatColor.GRAY + "Speed: " + ChatColor.GREEN + mount.getSpeed());

	if (mount.isOwnable()) {
	    lore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----" + ChatColor.RESET + ChatColor.DARK_GRAY
		    + "«" + ChatColor.AQUA + " Lore " + ChatColor.DARK_GRAY + "»" + ChatColor.DARK_GRAY
		    + ChatColor.STRIKETHROUGH + "----");
	    lore.add(ChatColor.DARK_GRAY + "A magical item known");
	    lore.add(ChatColor.DARK_GRAY + "to summon a personal");
	    lore.add(ChatColor.DARK_GRAY + "steed.");
	} else {
	    lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + mount.getPrice());
	    lore.add(ChatColor.GRAY + "Duration: " + ChatColor.GREEN + mount.getRentTime() + " mins");
	    lore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----" + ChatColor.RESET + ChatColor.DARK_GRAY
		    + "«" + ChatColor.AQUA + " Lore " + ChatColor.DARK_GRAY + "»" + ChatColor.DARK_GRAY
		    + ChatColor.STRIKETHROUGH + "----");
	    lore.add(ChatColor.DARK_GRAY + "Rent a mount for your");
	    lore.add(ChatColor.DARK_GRAY + "journey on the floating");
	    lore.add(ChatColor.DARK_GRAY + "castle of stone and steel.");
	}

	meta.setLore(lore);

	saddle.setItemMeta(meta);

	return saddle;
    }

    public HorseMount getMountFromID(String id) {
	for (Entry<HorseMount, ItemStack> entry : horseMountAndSaddleMap.entrySet()) {
	    if (entry.getKey().getID().equals(id)) {
		return entry.getKey();
	    }
	}
	return null;
    }

    public HorseMount getMountFromItem(ItemStack item) {
	for (Entry<HorseMount, ItemStack> entry : horseMountAndSaddleMap.entrySet()) {
	    String stackName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

	    if (ChatColor.stripColor(entry.getKey().getName()).equals(stackName)) {
		return entry.getKey();
	    }
	}
	return null;
    }

    public void reloadHorseFile() {
	horseFile = (YamlConfiguration) main.getConfig();
    }
}
