package creeoer.plugins.mounts.main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import creeoer.plugins.mounts.listeners.GUIListener;
import creeoer.plugins.mounts.listeners.HorseListener;
import creeoer.plugins.mounts.listeners.SaddleListener;

import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.objects.PlayerManager;
import creeoer.plugins.mounts.objects.RentChecker;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_15_R1.EntityHorse;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityTypes;


public class Mounts extends JavaPlugin {
	

	private YamlConfiguration horseFile;
	private YamlConfiguration playersFile;
	private MountLoader mountLoader;
	private PlayerManager playerManager;
	private YamlConfiguration currentHorsesFile;
	private static HashMap<UUID, HorseMount> currentHorsesInWorld;
	private static Set<MountEntity> horseEntitiesInWorld;
	public static Economy economy;
	private static int entityAmount = 0;
	
	
	
	 @Override
	 public void onEnable() {
		 
		 if(!getDataFolder().exists()) {
			 getDataFolder().mkdir();
			 saveDefaultConfig();
			 createPlayersFile();
			 createCurrentHorsesFile();
		 }
		 
		 currentHorsesInWorld = new HashMap<>();
		 horseEntitiesInWorld = new HashSet<>();
		 playersFile = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "players.yml"));
		 horseFile = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));
		 currentHorsesFile = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "horses.yml"));
	     registerEntity("Horse", 100 , EntityHorse.class , MountEntity.class);
		 
		 
		 
		 mountLoader = new MountLoader(this);
		 playerManager = new PlayerManager(this);
		 getCommand("mount").setExecutor(new Commands(this));
		 registerListeners();
		 new RentChecker(this).runTaskTimer(this, 40L, 100L);
	
		 setupEconomy();
		 
		 deserealizeAndSpawnEntities();
	 }
	 

	@Override
	 public void onDisable() {
		serealizeAllMountEntites();
		 
	 }
	 
	 public YamlConfiguration getHorseFile() {
		 return horseFile;
	 }

	public MountLoader getMountLoader() {
		return mountLoader;
	}
	
	public PlayerManager getPlayerManager() {
		return playerManager;
	}
	
	public YamlConfiguration getCurrentHorsesFile() {
		return currentHorsesFile;
	}
	
	public Set<MountEntity> getHorseEntitiesInWorld(){
		return horseEntitiesInWorld;
		}
	
	private void registerListeners() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new GUIListener(this), this);
		pm.registerEvents(new HorseListener(this), this);
		pm.registerEvents(new SaddleListener(this), this);
		
	}

	public YamlConfiguration getPlayersFile() {
		return playersFile;
	}
	 
	public void reloadConfigAndMounts() {
		reloadConfig();
		mountLoader.reloadHorseFile();
		mountLoader.loadHorseMountsAndCreateSaddles();
	}
	
	
	
	private void createPlayersFile() {
		File playerFile = new File(getDataFolder() + File.separator + "players.yml");
		try {
			playerFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		playersFile = YamlConfiguration.loadConfiguration(playerFile);
		playersFile.createSection("Players");
		try {
			playersFile.save(playerFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	 private void createCurrentHorsesFile() {
		// TODO Auto-generated method stub
		 File currentHorseFiles = new File(getDataFolder() + File.separator + "horses.yml");
			try {
				currentHorseFiles.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			currentHorsesFile = YamlConfiguration.loadConfiguration(currentHorseFiles);
			currentHorsesFile.createSection("Horses");
			try {
				currentHorsesFile.save(currentHorseFiles);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	


	public void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        try {
     
            List<Map<?, ?>> dataMap = new ArrayList<Map<?, ?>>();
            for (Field f : EntityTypes.class.getDeclaredFields()){
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())){
                    f.setAccessible(true);
                    dataMap.add((Map<?, ?>) f.get(null));
                }
            }
     
            if (dataMap.get(2).containsKey(id)){
                dataMap.get(0).remove(name);
                dataMap.get(2).remove(id);
            }
     
            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
 
    private boolean setupEconomy()
	    {
	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (economyProvider != null) {
	            economy = economyProvider.getProvider();
	        }

	        return (economy != null);
	    }
    
    private void serealizeAllMountEntites() {
			for(MountEntity entity: horseEntitiesInWorld) {
				        entityAmount++;
						serealizeMountEntity(entity);
						entity.getBukkitEntity().remove();
				
		}
		getLogger().info("Successfully saved all of the current horses");
    }
    
    private void deserealizeAndSpawnEntities() {
    	
    	if(currentHorsesFile.getConfigurationSection("Horses") == null || currentHorsesFile.getConfigurationSection("Horses").getKeys(false) == null)
    		return;
    		
    	for(String entry: currentHorsesFile.getConfigurationSection("Horses").getKeys(false)) {
    		Location loc = (Location) currentHorsesFile.get("Horses." +  entry + ".location");
    		HorseMount mount = mountLoader.getMountFromID(currentHorsesFile.getString("Horses." + entry + ".id"));
    		String ownerName =  currentHorsesFile.getString("Horses." +  entry + ".owner");
    		
    		
    	       loc.getChunk().load();
    		   Horse horse = MountEntity.spawn(loc, mount.getSpeed(), ownerName, mount);
    	
    		  
			   horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			   horse.setVariant(mount.getHorseType());
			   horse.getInventory().setArmor(mount.getArmor());
			   
			   if(mount.getHorseType() != Variant.SKELETON_HORSE || mount.getHorseType() != Variant.UNDEAD_HORSE)
			   horse.setColor(mount.getColor());
    		
			   
			   
    		currentHorsesFile.set("Horses." + entry , null);
    		currentHorsesInWorld.put(horse.getUniqueId(), mount);
    		
    		
    		EntityHorse horseEntity = ((CraftHorse) horse).getHandle();
    		horseEntitiesInWorld.add((MountEntity) horseEntity);
    
        	try {
    			currentHorsesFile.save(new File(getDataFolder() + File.separator + "horses.yml"));
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			getLogger().severe("There was a problem loading the horses");
    		}
        	
        	getLogger().info("Successfully loaded all player horses");
    		
    	}
    	
    	
    	
    }
    
    
    private void serealizeMountEntity(MountEntity mount) {
    	//save location, owner, and type
    	//number:
    	    //location:
    	    //owner:
    	    //id:
    	
    	
    	String ownerName = mount.getCustomName().getString().replace("'s Horse", "");
    	Location loc = mount.getBukkitEntity().getLocation();
    	
    	HorseMount mountType = retrieveHorseMountType(mount.getUniqueID());
    	String id = mountType.getID();
    	
    	currentHorsesFile.set("Horses." + entityAmount + ".location", loc);
    	currentHorsesFile.set("Horses." + entityAmount + ".id", id);
    	currentHorsesFile.set("Horses." + entityAmount + ".owner", ownerName);
    	try {
			currentHorsesFile.save(new File(getDataFolder() + File.separator + "horses.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	 
    
    public void registerHorseInWorld(UUID horseUUID, HorseMount mountType) {
    	currentHorsesInWorld.put(horseUUID, mountType);
    	horseEntitiesInWorld.add(HorseUtils.getHorseFromUUID(horseUUID));
    }
    
    public HorseMount retrieveHorseMountType(UUID horseUUID) {
    	return currentHorsesInWorld.get(horseUUID);
    	
    }
    
    public void removeHorseRegister(UUID horseUUID) {
    	currentHorsesInWorld.remove(horseUUID);
    }
    
    
    
    
    public void removeHorseFromHorseSet(MountEntity entity) {
    	horseEntitiesInWorld.remove(entity);
    }
    

}
