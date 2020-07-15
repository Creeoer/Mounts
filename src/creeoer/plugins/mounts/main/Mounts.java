package creeoer.plugins.mounts.main;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import creeoer.plugins.mounts.listeners.GUIListener;
import creeoer.plugins.mounts.listeners.HorseListener;
import creeoer.plugins.mounts.listeners.SaddleListener;
import creeoer.plugins.mounts.objects.DatabaseHandler;
import creeoer.plugins.mounts.objects.DatabaseWrapper;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.objects.PlayerManager;
import creeoer.plugins.mounts.objects.RentChecker;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_15_R1.EntityHorse;
import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityTypes;

public class Mounts extends JavaPlugin {
    private YamlConfiguration configFile;
    private YamlConfiguration playersFile;
    private MountLoader mountLoader;
    private PlayerManager playerManager;
    private YamlConfiguration currentHorsesFile;
    private static HashMap<UUID, HorseMount> currentHorsesInWorld;
    private static Set<MountEntity> horseEntitiesInWorld;
    private DatabaseHandler handler;
    private HorseLoader horseLoader;
    public static Economy economy;
    private static int entityAmount;
    public boolean usemysql;

    @Override
    public void onEnable() {

	if (!getDataFolder().exists())
	    createDataFolder();

	setupEconomy();
	currentHorsesInWorld = new HashMap<>();
	horseEntitiesInWorld = new HashSet<>();
	configFile = YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + "config.yml"));
	initDataMethod();
	registerEntity("Horse", 100, EntityHorse.class, MountEntity.class);
	horseLoader = new HorseLoader(this);

	mountLoader = new MountLoader(this);
	if (usemysql) {
	    playerManager = new DatabaseWrapper(this);
	} else {
	    playerManager = new PlayerManager(this);
	}
	getCommand("mount").setExecutor(new Commands(this));
	registerListeners();
	new RentChecker(this).runTaskTimer(this, 40L, 100L);

	deserealizeAndSpawnEntities();
    }

    @Override
    public void onDisable() {
	serealizeAllMountEntites();
	if (usemysql)
	    handler.closeConnection();

    }

    /**
     * Determines whether or not to use mysql or flat file storage
     */
    private void initDataMethod() {
	if (configFile.getBoolean("Database Settings.usemysql")) {
	    usemysql = true;
	    handler = new DatabaseHandler(this);
	} else {
	    currentHorsesFile = YamlConfiguration
		    .loadConfiguration(new File(getDataFolder() + File.separator + "horses.yml"));
	    playersFile = YamlConfiguration
		    .loadConfiguration(new File(getDataFolder() + File.separator + "players.yml"));
	}
    }

    private void createDataFolder() {
	getDataFolder().mkdir();
	saveDefaultConfig();
	createPlayersFile();
	createCurrentHorsesFile();
    }

    public DatabaseHandler getDatabaseHandler() {
	return handler;
    }

    public YamlConfiguration getConfigFile() {
	return configFile;
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

    public Set<MountEntity> getHorseEntitiesInWorld() {
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
	    e.printStackTrace();
	}

	playersFile = YamlConfiguration.loadConfiguration(playerFile);
	playersFile.createSection("Players");
	try {
	    playersFile.save(playerFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void createCurrentHorsesFile() {
	File currentHorseFiles = new File(getDataFolder() + File.separator + "horses.yml");
	try {
	    currentHorseFiles.createNewFile();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	currentHorsesFile = YamlConfiguration.loadConfiguration(currentHorseFiles);
	currentHorsesFile.createSection("Horses");
	try {
	    currentHorsesFile.save(currentHorseFiles);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass,
	    Class<? extends EntityInsentient> customClass) {
	try {
	    List<Map<?, ?>> dataMap = new ArrayList<>();
	    for (Field f : EntityTypes.class.getDeclaredFields()) {
		if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
		    f.setAccessible(true);
		    dataMap.add((Map<?, ?>) f.get(null));
		}
	    }

	    if (dataMap.isEmpty()) {
		return;
	    }

	    if (dataMap.get(2).containsKey(id)) {
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

    private boolean setupEconomy() {
	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
		.getRegistration(Economy.class);
	if (economyProvider != null) {
	    economy = economyProvider.getProvider();
	}

	return economy != null;
    }

    private void serealizeAllMountEntites() {
	for (MountEntity entity : horseEntitiesInWorld) {
	    entityAmount++;
	    serealizeMountEntity(entity);
	    entity.getBukkitEntity().remove();
	}
	getLogger().info("Successfully saved all of the current horses");
    }

    private void deserealizeAndSpawnEntities() {
	try {
	    Location loc;
	    HorseMount mount;
	    String ownerName;

	    if (usemysql) {
		Connection conn = handler.getConnection();
		Statement statement = conn.createStatement();
		ResultSet set = statement.executeQuery("SELECT * FROM horses");

		deleteEntityDataFromTable(statement, set);

	    } else {

		if (currentHorsesFile.getConfigurationSection("Horses") == null
			|| currentHorsesFile.getConfigurationSection("Horses").getKeys(false) == null) {
		    return;
		}
		for (String entry : currentHorsesFile.getConfigurationSection("Horses").getKeys(false)) {
		    loc = (Location) currentHorsesFile.get("Horses." + entry + ".location");
		    mount = mountLoader.getMountFromID(currentHorsesFile.getString("Horses." + entry + ".id"));
		    ownerName = currentHorsesFile.getString("Horses." + entry + ".owner");

		    horseLoader.loadHorseIntoWorld(loc, mount, ownerName);
		    currentHorsesFile.set("Horses." + entry, null);
		}

		currentHorsesFile.save(new File(getDataFolder() + File.separator + "horses.yml"));
	    }
	} catch (Exception e) {
	    getLogger().severe("There was a problem loading the horses");
	}

	getLogger().info("Successfully loaded all player horses");
    }

    private void deleteEntityDataFromTable(Statement statement, ResultSet set) throws SQLException {
	Location loc;
	HorseMount mount;
	String ownerName;
	while (set.next()) {
	    loc = new Location(getServer().getWorld(set.getString(2)), set.getDouble(3), set.getDouble(4),
		    set.getDouble(5), set.getFloat(6), set.getFloat(7));
	    mount = mountLoader.getMountFromID(set.getString(8));
	    ownerName = set.getString(9);
	    horseLoader.loadHorseIntoWorld(loc, mount, ownerName);
	    statement.executeUpdate("DELETE FROM horses WHERE horseNumber = '" + set.getInt(0) + "'");
	}
    }

    private void serealizeMountEntity(MountEntity mount) {
	// save location, owner, and type
	// number:
	// location:
	// owner:
	// id:
	String ownerName = mount.getCustomName().getString().replace("'s Horse", "");
	Location loc = mount.getBukkitEntity().getLocation();

	HorseMount mountType = retrieveHorseMountType(mount.getUniqueID());
	String id = mountType.getID();

	try {
	    if (usemysql) {
		Connection conn = handler.getConnection();
		Statement statement = conn.createStatement();

		statement.executeUpdate(
			"INSERT INTO horses (horseNumber, world, x, y, z, yaw, pitch, id, owner) VALUES (" + "'"
				+ entityAmount + "'" + ", " + "'" + loc.getWorld().getName() + "'" + ", " + "'"
				+ loc.getX() + "', " + "'" + loc.getY() + "', " + "'" + loc.getZ() + "', " + "'"
				+ loc.getYaw() + "', " + "'" + loc.getPitch() + "', " + "'" + id + "', " + "'"
				+ ownerName + "')");
		return;
	    }

	    currentHorsesFile.set("Horses." + entityAmount + ".location", loc);
	    currentHorsesFile.set("Horses." + entityAmount + ".id", id);
	    currentHorsesFile.set("Horses." + entityAmount + ".owner", ownerName);

	    currentHorsesFile.save(new File(getDataFolder() + File.separator + "horses.yml"));
	} catch (Exception e) {
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

    public void removeHorseFromRegisterAndSet(MountEntity entity) {
	removeHorseFromHorseSet(entity);
	removeHorseRegister(entity.getUniqueID());
    }

    public void removeHorseRegister(UUID horseUUID) {
	currentHorsesInWorld.remove(horseUUID);
    }

    public HorseLoader getHorseLoader() {
	return horseLoader;
    }

    public void removeHorseFromHorseSet(MountEntity entity) {
	horseEntitiesInWorld.remove(entity);
    }
}
