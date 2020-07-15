package creeoer.plugins.mounts.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.objects.HorseMount;

//This is an alternative version of the PlayerManager class that uses mysql for all data handling
public class DatabaseWrapper extends PlayerManager {

    public DatabaseWrapper(Mounts pluginInstance) {
	super(pluginInstance);
    }

    @Override
    protected void loadCurrentRenters() {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		try {
		    Statement statement = handler.getStatement();
		    ResultSet playerEntries = statement.executeQuery("SELECT playerName, timeBought FROM renters");

		    while (playerEntries.next())
			currentRenters.put(playerEntries.getString(1), playerEntries.getLong(2));

		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}.runTaskAsynchronously(main);

    }

    @Override
    public void serealizeMountEntity(String ownerName, Location loc, String id) {
	Statement statement = handler.getStatement();
	try {
	    statement.executeUpdate("INSERT INTO horses (horseNumber, world, x, y, z, yaw, pitch, id, owner) VALUES ("
		    + "'" + main.entityAmount + "'" + ", " + "'" + loc.getWorld().getName() + "'" + ", " + "'"
		    + loc.getX() + "', " + "'" + loc.getY() + "', " + "'" + loc.getZ() + "', " + "'" + loc.getYaw()
		    + "', " + "'" + loc.getPitch() + "', " + "'" + id + "', " + "'" + ownerName + "')");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void deserealizeAndSpawnMountEntities() {
	try {
	    Statement statement = handler.getStatement();
	    ResultSet set = statement.executeQuery("SELECT * FROM horses");
	    Location loc;
	    HorseMount mount;
	    String ownerName;
	    while (set.next()) {
		loc = new Location(main.getServer().getWorld(set.getString(2)), set.getDouble(3), set.getDouble(4),
			set.getDouble(5), set.getFloat(6), set.getFloat(7));
		mount = saddleHandler.getMountFromID(set.getString(8));
		ownerName = set.getString(9);
		horseLoader.loadHorseIntoWorld(loc, mount, ownerName);
		statement.executeUpdate("DELETE FROM horses WHERE horseNumber = '" + set.getInt(0) + "'");
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void addRenter(String playerName, String horseID) {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		long timeBought = System.currentTimeMillis();
		try {
		    Statement statement = handler.getStatement();
		    statement.executeUpdate("INSERT INTO renters (playerName, horseID, timeBought) VALUES (" + "'"
			    + playerName + "', " + "'" + horseID + "' " + ", " + " '" + timeBought + "')");
		} catch (Exception e) {
		    e.printStackTrace();
		}
		currentRenters.put(playerName, timeBought);
	    }
	}.runTaskAsynchronously(main);
    }

    @Override
    public void removeRenter(String playerName) {
	new BukkitRunnable() {
	    @Override
	    public void run() {
		try {
		    Statement statement = handler.getStatement();

		    statement.executeUpdate("DELETE FROM renters WHERE playerName = '" + playerName + " ' ");
		} catch (Exception e) {
		    e.printStackTrace();
		}
		currentRenters.remove(playerName);
	    }
	}.runTaskAsynchronously(main);
    }

    @Override
    public String getPlayerHorseID(String playerName) {
	Statement statement = handler.getStatement();
	try {
	    ResultSet set = statement
		    .executeQuery("SELECT horseID FROM renters WHERE playerName = '" + playerName + "'");

	    while (set.next())
		return set.getString("horseID");

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return null;
    }
}
