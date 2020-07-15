package creeoer.plugins.mounts.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Mounts;

public class DatabaseWrapper extends PlayerManager {

    public DatabaseWrapper(Mounts pluginInstance) {
	super(pluginInstance);

    }

    @Override
    void loadCurrentRenters() {
	new BukkitRunnable() {
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

    public void addRenter(String playerName, String horseID) {
	new BukkitRunnable() {
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

    public void removeRenter(String playerName) {
	new BukkitRunnable() {
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
