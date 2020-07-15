package creeoer.plugins.mounts.objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.MountLoader;
import creeoer.plugins.mounts.main.Mounts;
import net.md_5.bungee.api.ChatColor;

public class ReturnHorseTask extends BukkitRunnable {
    private MountLoader mountLoader;
    private PlayerManager playerManager;
    private MountEntity mountEntity;
    private Mounts main;

    public ReturnHorseTask(Mounts main, MountEntity entity) {
	this.main = main;
	mountLoader = main.getMountLoader();
	this.mountEntity = entity;
	this.playerManager = main.getPlayerManager();
    }

    @Override
    public void run() {
	// loop through all horses..if horse is ownable return its item to player and
	// send message
	if (mountEntity.getBukkitEntity().getPassenger() == null && mountEntity.isAlive()) {
	    // get owner...
	    String ownerName = mountEntity.getCustomName().getString().replace("'s Horse", "");
	    HorseMount mountType = main.retrieveHorseMountType(mountEntity.getUniqueID());
	    ItemStack stack = mountLoader.getHorseMountAndSaddleMap().get(mountType);
	    stack.setType(Material.SADDLE);
	    OfflinePlayer player = Bukkit.getOfflinePlayer(ownerName);
	    if (!player.isOnline()) {
		return;
	    }
	    player.getPlayer().getInventory().addItem(stack);
	    playerManager.removeHorseEntity(mountEntity);
	    player.getPlayer().sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY
		    + "Your horse was not being ridden and was given back to you.");
	}
    }
}
