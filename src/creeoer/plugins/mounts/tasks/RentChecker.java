package creeoer.plugins.mounts.tasks;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.MountLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.mysql.PlayerManager;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import net.md_5.bungee.api.ChatColor;

public class RentChecker extends BukkitRunnable {
    private PlayerManager playerManager;
    private MountLoader mountLoader;
    private Mounts main;

    public RentChecker(Mounts main) {
	playerManager = main.getPlayerManager();
	mountLoader = main.getMountLoader();
	this.main = main;
    }

    private static HashMap<String, Long> rentTimes = new HashMap<>();

    @Override
    public void run() {
	// go through owners list and check times
	for (String owner : playerManager.getCurrentRenters()) {
	    long timeBought;
	    long rentTime = mountLoader.getMountFromID(playerManager.getPlayerHorseID(owner)).getRentTime();

	    if (rentTimes.get(owner) != null) {
		timeBought = rentTimes.get(owner);
	    } else {
		timeBought = playerManager.getTimeBought(owner);
	    }

	    if (timeBought / 1000 + rentTime * 60 <= System.currentTimeMillis() / 1000) {
		playerManager.removeRenter(owner);

		if (rentTimes.get(owner) != null) {
		    rentTimes.remove(owner);
		}

		List<MountEntity> playerHorses = playerManager.getPlayerHorsesInWorld(owner);

		if (!playerHorses.isEmpty()) {
		    for (MountEntity playerHorse : playerHorses) {
			HorseMount mountType = main.retrieveHorseMountType(playerHorse.getUniqueID());

			if (!mountType.isOwnable()) {
			    playerManager.removeHorseEntity(playerHorse);
			    break;
			}
		    }
		}

		OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
		if (player.isOnline()) {
		    Player playerOnline = Bukkit.getPlayer(owner);
		    playerOnline.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY
			    + "Your rent has ran out and your horse has despawned");
		}
	    } else {
		rentTimes.put(owner, timeBought);
	    }
	}

    }
}
