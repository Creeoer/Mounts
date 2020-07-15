package creeoer.plugins.mounts.objects;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Mounts;
import net.minecraft.server.v1_15_R1.EntityHorse;

public class PlayerManager {
    private HashMap<String, Long> currentRenters;
    private YamlConfiguration playersFile;
    private Mounts main;
    private List<UUID> deletionQueue;
    private boolean usingSql;
    private DatabaseHandler handler;

    public PlayerManager(Mounts pluginInstance) {
	main = pluginInstance;
	usingSql = main.usemysql;

	currentRenters = new HashMap<>();
	deletionQueue = new ArrayList<>();
	handler = main.getDatabaseHandler();

	loadCurrentRenters();

    }

    private void loadCurrentRenters() {
	new BukkitRunnable() {
	    public void run() {
		if (usingSql) {
		    Connection conn = handler.getConnection();
		    Statement statement;
		    try {
			statement = conn.createStatement();
			ResultSet playerEntries = statement.executeQuery("SELECT playerName, timeBought FROM renters");

			while (playerEntries.next()) {
			    currentRenters.put(playerEntries.getString(1), playerEntries.getLong(2));
			}
		    } catch (SQLException e) {
			e.printStackTrace();
		    }
		    return;
		}

		try {
		    for (String playerEntry : playersFile.getConfigurationSection("Players").getKeys(false)) {
			currentRenters.put(playerEntry, playersFile.getLong("Players." + playerEntry + ".timeBought"));
		    }
		} catch (NullPointerException e) {

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
		    if (usingSql) {
			Connection conn = handler.getConnection();
			Statement statement;
			statement = conn.createStatement();
			statement.executeUpdate("INSERT INTO renters (playerName, horseID, timeBought) VALUES (" + " '"
				+ horseID + "' " + ", " + " '" + timeBought + "')");
		    } else {
			playersFile.set("Players." + playerName + ".horseID", horseID);
			playersFile.set("Players." + playerName + ".timeBought", System.currentTimeMillis());
			playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		    }
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

	EntityHorse horse = entity;
	Horse buukkitHorse = (Horse) horse.getBukkitEntity();

	buukkitHorse.remove();
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
		    if (usingSql) {
			Connection conn = handler.getConnection();
			Statement statement;
			statement = conn.createStatement();
			statement.executeUpdate("DELETE FROM renters WHERE playerName = '" + playerName + " ' ");
		    } else {
			playersFile.set("Players." + playerName, null);

			playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		currentRenters.remove(playerName);
	    }
	}.runTaskAsynchronously(main);
    }

    public String getPlayerHorseID(String playerName) {
	if (usingSql) {
	    Connection conn = handler.getConnection();
	    Statement statement;
	    try {
		statement = conn.createStatement();
		ResultSet set = statement
			.executeQuery("SELECT horseID from renters WHERE playerName = '" + playerName + "'");
		return set.toString();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}

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
