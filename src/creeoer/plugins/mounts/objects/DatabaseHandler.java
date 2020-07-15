package creeoer.plugins.mounts.objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.YamlConfiguration;

import creeoer.plugins.mounts.main.Mounts;

public class DatabaseHandler {
    private Mounts main;
    private Connection connection;
    private static String host, database, user, password;
    private static int port;

    public DatabaseHandler(Mounts pluginInstance) {
	main = pluginInstance;
	initSettings();
	attemptConnection();

	try {
	    if (!checkIfTablesExist()) {
		createTables();
	    }
	} catch (SQLException e) {
	    attemptConnection();
	    e.printStackTrace();
	}
    }

    public void attemptConnection() {
	try {
	    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user,
		    password);
	} catch (SQLException e) {
	    main.getLogger().info("Problem loading mysql! Disabling plugin functionality...");
	    main.getPluginLoader().disablePlugin(main);
	    e.printStackTrace();
	}
    }

    public Connection getConnection() {
	try {
	    if (connection != null && !connection.isClosed()) {
		return connection;
	    }
	} catch (SQLException e) {
	    attemptConnection();
	}

	return connection;
    }

    public void closeConnection() {
	try {
	    if (connection == null)
		return;

	    if (!connection.isClosed()) {
		connection.close();
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    public boolean checkIfTablesExist() throws SQLException {
	Statement statement = connection.createStatement();
	ResultSet result = statement.executeQuery("SHOW TABLES LIKE 'renters'");
	return result.isBeforeFirst();
    }

    public void initSettings() {
	YamlConfiguration config = main.getConfigFile();
	String settings = "Database Settings.";
	host = config.getString(settings + "host");
	database = config.getString(settings + "databaseName");
	user = config.getString(settings + "user");
	password = config.getString(settings + "pass");
	port = config.getInt(settings + "port");
    }

    public void createTables() throws SQLException {
	String rentersTable = "CREATE TABLE renters " + "(playerName VARCHAR(255), " + " horseID VARCHAR(255), "
		+ " timeBought LONG, " + " PRIMARY KEY ( playerName))";

	String horsesTable = "CREATE TABLE horses" + "(horseNumber INTEGER, " + " world VARCHAR(255), " + " x DOUBLE, "
		+ " y DOUBLE, " + " z DOUBLE, " + " yaw FLOAT, " + " pitch FLOAT, " + " id VARCHAR(255), "
		+ " owner VARCHAR(255), " + " PRIMARY KEY ( horseNumber ))";
	Statement statement = connection.createStatement();
	statement.executeUpdate(rentersTable);
	statement.executeUpdate(horsesTable);
    }
}
