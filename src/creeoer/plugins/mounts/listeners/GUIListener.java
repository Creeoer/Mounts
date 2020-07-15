package creeoer.plugins.mounts.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.HorseLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.main.SaddleHandler;
import creeoer.plugins.mounts.mysql.PlayerManager;
import creeoer.plugins.mounts.objects.HorseMount;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class GUIListener implements Listener {
    private SaddleHandler saddleHandler;
    private PlayerManager playerManager;
    private Mounts main;
    private HorseLoader horseLoader;

    public GUIListener(Mounts pluginInstance) {
	main = pluginInstance;
	saddleHandler = pluginInstance.getSaddleHandler();
	playerManager = pluginInstance.getPlayerManager();
	horseLoader = pluginInstance.getHorseLoader();
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {

	if (event.getView().getTitle().isEmpty())
	    return;

	if (event.getView().getTitle().contains("Rent A Horse") && event.getCurrentItem().getType() != Material.AIR) {
	    event.setCancelled(true);

	    ItemStack clickedItem = event.getCurrentItem();

	    HorseMount mount = saddleHandler.getMountFromItem(clickedItem);

	    Player player = (Player) event.getWhoClicked();

	    EconomyResponse trans = main.economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()),
		    mount.getPrice());

	    if (!trans.transactionSuccess()) {
		player.closeInventory();
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You can not afford to rent/buy this horse");
		return;
	    }

	    if (playerManager.isRenting(player.getName()) && !mount.isOwnable()) {
		event.getWhoClicked().sendMessage(ChatColor.RED + "You're already renting a horse");
		return;
	    }

	    if (mount.isOwnable()) {
		// Gets saddle itemstack that corresponds to GUI item
		ItemStack saddle = saddleHandler.getHorseMountAndSaddleMap().get(mount);
		saddle.setType(Material.SADDLE);
		player.getInventory().addItem(saddle);
		player.updateInventory();
		player.closeInventory();
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You have successfully bought this horse");
	    } else {
		playerManager.addRenter(event.getWhoClicked().getName(), mount.getID());
		player.closeInventory();
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You have successfully rented a horse");
		spawnHorse(player, mount);
	    }
	}

    }

    private void spawnHorse(Player player, HorseMount mount) {
	horseLoader.loadHorseIntoWorld(player.getLocation(), mount, player.getName());
    }
}
