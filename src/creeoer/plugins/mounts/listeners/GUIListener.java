package creeoer.plugins.mounts.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.HorseLoader;
import creeoer.plugins.mounts.main.MountLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.objects.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class GUIListener implements Listener {
    private MountLoader mountLoader;
    private PlayerManager playerManager;
    private Mounts main;
    private HorseLoader horseLoader;

    public GUIListener(Mounts pluginInstance) {
	main = pluginInstance;
	mountLoader = pluginInstance.getMountLoader();
	playerManager = pluginInstance.getPlayerManager();
	horseLoader = pluginInstance.getHorseLoader();
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
	try {
	    if (event.getView().getTitle() == null || event.getView().getTitle() == null) {
		return;
	    }

	    if (event.getView().getTitle().contains("Rent A Horse")) {
		event.setCancelled(true);

		String playerName = event.getWhoClicked().getName();

		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem.getType() == Material.AIR) {
		    main.getLogger().info("after clickedItem get type AIR");
		    return;
		}
		main.getLogger().info("after clickedItem get type");

		HorseMount mount = mountLoader.getMountFromItem(clickedItem);

		if (mount == null) {
		    Bukkit.broadcastMessage("null");
		    main.getLogger().info("nuull on mount = null");
		    return;
		}

		Player player = (Player) event.getWhoClicked();

		EconomyResponse trans = main.economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()),
			mount.getPrice());
		main.getLogger().info("after trans response");
		if (!trans.transactionSuccess()) {
		    player.closeInventory();
		    player.sendMessage(
			    Commands.MOUNT_PREFIX + ChatColor.RED + "You can not afford to rent/buy this horse");
		    return;
		}

		if (playerManager.isRenting(playerName) && !mount.isOwnable()) {
		    event.getWhoClicked().sendMessage(ChatColor.RED + "You're already renting a horse");
		    return;
		}

		if (mount.isOwnable()) {
		    ItemStack saddle = mountLoader.getHorseMountAndSaddleMap().get(mount);
		    saddle.setType(Material.SADDLE);
		    player.getInventory().addItem(saddle);
		    player.updateInventory();
		    player.closeInventory();
		    player.sendMessage(
			    Commands.MOUNT_PREFIX + ChatColor.GRAY + "You have successfully bought this horse");
		} else {
		    playerManager.addRenter(event.getWhoClicked().getName(), mount.getID());
		    player.closeInventory();
		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You have successfully rented a horse");
		    spawnHorse(player, mount);

		}
	    }
	} catch (NullPointerException e) {
	}
    }

    @EventHandler
    public void onUnrentGUIClick(InventoryClickEvent event) {
	try {
	    if (event.getView().getTitle() == null) {
		return;
	    }

	    if (event.getView().getTitle().contains("Unrent Your Horse")) {
		event.setCancelled(true);

		if (event.getCurrentItem() == null) {
		    return;
		}

		Player player = (Player) event.getWhoClicked();

		if (event.getCurrentItem().getType() == Material.BARRIER) {
		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "Unrenting cancelled");
		} else if (event.getCurrentItem().getType() == Material.POISONOUS_POTATO) {
		    HorseMount mount = null;
		    MountEntity horse = null;

		    List<MountEntity> mounts = playerManager.getPlayerHorsesInWorld(player.getName());
		    for (MountEntity entity : mounts) {
			HorseMount mountType = main.retrieveHorseMountType(entity.getUniqueID());
			if (!mountType.isOwnable()) {
			    horse = entity;
			    mount = entity.getMountType();
			    break;
			}
		    }

		    Mounts.economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), mount.getPrice() / 2);
		    playerManager.removeHorseEntity(horse);
		    playerManager.removeRenter(player.getName());

		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You unrented " + ChatColor.GREEN
			    + mount.getName() + ChatColor.GRAY + " for " + ChatColor.GREEN + mount.getPrice() / 2
			    + ChatColor.GRAY + " cors");
		}

		player.closeInventory();
	    }
	} catch (NullPointerException e) {
	}
    }

    private void spawnHorse(Player player, HorseMount mount) {
	horseLoader.loadHorseIntoWorld(player.getLocation(), mount, player.getName());
    }
}
