package creeoer.plugins.mounts.mysql;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.objects.MountEntity;

public class PlayerManager {
    HashMap<String, Long> currentRenters;
    YamlConfiguration playersFile;
    Mounts main;
    List<UUID> deletionQueue;
    boolean usingSql;
    DatabaseHandler handler;

    public PlayerManager(Mounts pluginInstance) {
	main = pluginInstance;
	usingSql = main.usemysql;

	currentRenters = new HashMap<>();
	deletionQueue = new ArrayList<>();
	handler = main.getDatabaseHandler();
	loadCurrentRenters();

    }

    protected void loadCurrentRenters() {
	new BukkitRunnable() {
	    public void run() {
		try {
		    for (String playerEntry : playersFile.getConfigurationSection("Players").getKeys(false))
			currentRenters.put(playerEntry, playersFile.getLong("Players." + playerEntry + ".timeBought"));

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
}
