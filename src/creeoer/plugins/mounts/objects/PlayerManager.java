package creeoer.plugins.mounts.objects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

import creeoer.plugins.mounts.main.Mounts;
import net.minecraft.server.v1_15_R1.EntityHorse;

public class PlayerManager {
	
	private Set<String> currentRenters;
	private YamlConfiguration playersFile;
	private Mounts main;
	private List<UUID> deletionQueue;

	
	
	/*
	 * playerName:
	 *    timeBought: 
	 *    horseID: 
	 */
	
	//do not instiainate this 
	public PlayerManager(Mounts pluginInstance) {
		main = pluginInstance;
		playersFile = pluginInstance.getPlayersFile();
		currentRenters = new HashSet<>();
		deletionQueue = new ArrayList<>();

		loadCurrentOwners();
		//load players & load horses in world
		
	}
	
	//create method to add all owners from file
	
	//this class handles managing what players own what horse
	
	
	
	
	private void loadCurrentOwners() {
		
	try {
		for(String playerEntry: playersFile.getConfigurationSection("Players").getKeys(false)) {
			 currentRenters.add(playerEntry);
		}		
	} catch (NullPointerException e) {}
		
	}
	
	public Set<String> getCurrentRenters() {
		return currentRenters;
	}
	
	public long getTimeBought(String renterName) {
		for(String playerEntries: playersFile.getConfigurationSection("Players").getKeys(false)) {
			if(playerEntries.equals(renterName)) {
				long timeBought = playersFile.getLong("Players." + renterName + ".timeBought");
				return timeBought;
			}
			
		}
		return 0;
	}
	
	
	public void addRenter(String playerName, String horseID) {
		
		playersFile.set("Players." + playerName + ".horseID", horseID );
		playersFile.set("Players." + playerName + ".timeBought", System.currentTimeMillis());
		try {
			playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		currentRenters.add(playerName);
	
	}
	
	public void removeHorseEntity(MountEntity entity, String ownerName) 
	{
				
		main.removeHorseRegister(entity.getUniqueID());
		main.removeHorseFromHorseSet(entity);
		deletionQueue.add(entity.getUniqueID());
		
		EntityHorse horse = (EntityHorse) entity;
		Horse buukkitHorse = (Horse) horse.getBukkitEntity();

		
		buukkitHorse.remove();
	

	}
	
	public List<UUID> getHorsesToBeDeleted(){
		return deletionQueue;
	}
	
	public List<MountEntity> getPlayerHorsesInWorld(String playerName) {
	List<MountEntity> mountEntites = new ArrayList<>();
	
		for (MountEntity entity: main.getHorseEntitiesInWorld()) {
	     		String horseName = entity.getCustomName().getString();
				String horsePlayerName = playerName + "'s" + " Horse";
					
					if(horseName.equals(horsePlayerName)) 
						mountEntites.add(entity);
				
			}
		return mountEntites;
	}
	
	
	public boolean isRenting(String playerName) {
		
		if(currentRenters.contains(playerName))
			return true;
		
		return false;
	}
	
	
	public void removeRenter(String playerName) {
		playersFile.set("Players." + playerName, null);
		try {
			playersFile.save(new File(main.getDataFolder() + File.separator + "players.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentRenters.remove(playerName);
		
	}
	
	
	public String getPlayerHorseID(String playerName) {
		
		for(String playerEntries: playersFile.getConfigurationSection("Players").getKeys(false)) {
			if(playerEntries.equals(playerName)) {
				String id = playersFile.getString("Players." + playerName + ".horseID");
				return id;
			}
			
		}
		return null;
		
	}

	public void removeHorseFromQueue(UUID horseID) {
		deletionQueue.remove(horseID);
		
	}
	
	
	
	

	
}
