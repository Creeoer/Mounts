package creeoer.plugins.mounts.mysql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.HorseLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.main.SaddleHandler;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import net.md_5.bungee.api.ChatColor;

public class PlayerManager {
    protected HashMap<String, Long> currentRenters;
    protected YamlConfiguration playersFile;
    protected YamlConfiguration currentHorsesFile;
    protected Mounts main;
    protected List<UUID> deletionQueue;
    protected boolean usingSql;
    protected DatabaseHandler handler;
    protected HorseLoader horseLoader;
    protected SaddleHandler saddleHandler;

    public PlayerManager(Mounts pluginInstance) {
	main = pluginInstance;
	usingSql = main.usemysql;

	currentRenters = new HashMap<>();
	deletionQueue = new ArrayList<>();
	handler = main.getDatabaseHandler();
	currentHorsesFile = main.getCurrentHorsesFile();
	horseLoader = main.getHorseLoader();
	saddleHandler = main.getSaddleHandler();
	loadCurrentRenters();
    }

    protected void loadCurrentRenters() {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		try {
		    for (String playerEntry : playersFile.getConfigurationSection("Players").getKeys(false)) {
			currentRenters.put(playerEntry, playersFile.getLong("Players." + playerEntry + ".timeBought"));
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}.runTaskAsynchronously(main);
    }

    public Set<String> getCurrentRenters() {
	return currentRenters.keySet();
    }

    public long getTimeBought(String renterName) {
	if (currentRenters.get(renterName) != null) {
	    return currentRenters.get(renterName);
	}

	return 0;
    }

    public void addRenter(String playerName, String horseID) {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		long timeBought = System.currentTimeMillis();

		try {
		    playersFile.set("Players." + playerName + ".horseID", horseID);
		    playersFile.set("Players." + playerName + ".timeBought", timeBought);
		    playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		} catch (Exception e) {
		    e.printStackTrace();
		}
		currentRenters.put(playerName, timeBought);
	    }
	}.runTaskAsynchronously(main);
    }

    public void removeHorseEntity(MountEntity entity) {
	main.removeHorseFromRegisterAndSet(entity);
	deletionQueue.add(entity.getUniqueID());

	entity.getBukkitEntity().remove();
    }

    public List<UUID> getHorsesToBeDeleted() {
	return deletionQueue;
    }

    public List<MountEntity> getPlayerHorsesInWorld(String playerName) {
	List<MountEntity> mountEntites = new ArrayList<>();

	for (MountEntity entity : main.getHorseEntitiesInWorld()) {
	    String horseName = entity.getCustomName().getString();
	    String horsePlayerName = playerName + "'s" + " Horse";

	    if (horseName.equals(horsePlayerName)) {
		mountEntites.add(entity);
	    }
	}
	return mountEntites;
    }

    public boolean isRenting(String playerName) {
	return currentRenters.containsKey(playerName);
    }

    public void removeRenter(String playerName) {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		try {
		    playersFile.set("Players." + playerName, null);
		    playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		} catch (Exception e) {
		    e.printStackTrace();
		}

		currentRenters.remove(playerName);
	    }
	}.runTaskAsynchronously(main);
    }

    public void serealizeMountEntity(String ownerName, Location loc, String id) {
	currentHorsesFile.set("Horses." + main.entityAmount + ".location", loc);
	currentHorsesFile.set("Horses." + main.entityAmount + ".id", id);
	currentHorsesFile.set("Horses." + main.entityAmount + ".owner", ownerName);

	try {
	    currentHorsesFile.save(new File(main.getDataFolder() + File.separator + "horses.yml"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void deserealizeAndSpawnMountEntities() {
	Location loc;
	HorseMount mount;
	String ownerName;
	if (currentHorsesFile.getConfigurationSection("Horses") == null
		|| currentHorsesFile.getConfigurationSection("Horses").getKeys(false) == null) {
	    return;
	}
	for (String entry : currentHorsesFile.getConfigurationSection("Horses").getKeys(false)) {
	    loc = (Location) currentHorsesFile.get("Horses." + entry + ".location");
	    mount = saddleHandler.getMountFromID(currentHorsesFile.getString("Horses." + entry + ".id"));
	    ownerName = currentHorsesFile.getString("Horses." + entry + ".owner");

	    horseLoader.loadHorseIntoWorld(loc, mount, ownerName);
	    currentHorsesFile.set("Horses." + entry, null);
	}

	try {
	    currentHorsesFile.save(new File(main.getDataFolder() + File.separator + "horses.yml"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public String getPlayerHorseID(String playerName) {
	for (String playerEntries : playersFile.getConfigurationSection("Players").getKeys(false)) {
	    if (playerEntries.equals(playerName)) {
		return playersFile.getString("Players." + playerName + ".horseID");
	    }
	}
	return null;
    }

    public void removeHorseFromQueue(UUID horseID) {
	deletionQueue.remove(horseID);
    }

    @EventHandler
    public void onUnrentGUIClick(InventoryClickEvent event, Mounts main) {
	if (event.getView().getTitle().isEmpty())
	    return;

	if (event.getView().getTitle().contains("Unrent Your Horse")) {
	    event.setCancelled(true);
	    
	    if (event.getCurrentItem() == null) 
		return;
	    
	    Player player = (Player) event.getWhoClicked();
	    if (event.getCurrentItem().getType() == Material.BARRIER) {
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "Unrenting cancelled");
	    } else if (event.getCurrentItem().getType() == Material.POISONOUS_POTATO) {
		HorseMount mount = null;
		MountEntity horse = null;
		//Loops through current mounts in world and finds one that is attached to player by UUID
		List<MountEntity> mounts = getPlayerHorsesInWorld(player.getName());
		for (MountEntity entity : mounts) {
		    HorseMount mountType = main.retrieveHorseMountType(entity.getUniqueID());
		    if (!mountType.isOwnable()) {
			horse = entity;
			mount = entity.getMountType();
			break;
		    }
		}
		if(mount == null) {
		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "Couldn't unrent, no horse in world!");
		    return;
		}
		
		Mounts.economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), mount.getPrice() / 2);
		removeHorseEntity(horse);
		removeRenter(player.getName());
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You unrented " + ChatColor.GREEN
			+ mount.getName() + ChatColor.GRAY + " for " + ChatColor.GREEN + mount.getPrice() / 2
			+ ChatColor.GRAY + " cors");
	    }
	    player.closeInventory();
	}

    }
}
