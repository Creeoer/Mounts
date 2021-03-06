package creeoer.plugins.mounts.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.HorseLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.main.SaddleHandler;
import creeoer.plugins.mounts.mysql.PlayerManager;
import creeoer.plugins.mounts.objects.HorseMount;
import net.md_5.bungee.api.ChatColor;

public class SaddleListener implements Listener {
    private SaddleHandler saddleHandler;
    private PlayerManager playerManager;
    private HorseLoader horseLoader;

    public SaddleListener(Mounts main) {
	saddleHandler = main.getSaddleHandler();
	playerManager = main.getPlayerManager();

	horseLoader = main.getHorseLoader();
    }

    @EventHandler
    public void onSaddleClick(PlayerInteractEvent event) {
	if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		&& event.getPlayer().getItemInHand().getType() == Material.SADDLE
		&& event.getPlayer().getItemInHand().hasItemMeta()) {
	    HorseMount mount = saddleHandler.getMountFromItem(event.getPlayer().getItemInHand());

	    if (mount != null) {
		Player player = event.getPlayer();
		event.setCancelled(true);

		if (playerManager.isRenting(player.getName())) {
		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED
			    + "You can't spawn your owned horse since you are currently renting a horse");
		    return;
		}

		if (!playerManager.getPlayerHorsesInWorld(player.getName()).isEmpty()) {
		    player.sendMessage(
			    Commands.MOUNT_PREFIX + ChatColor.RED + "You can't spawn another horse with one summoned!");
		    return;
		}

		if (event.getClickedBlock() != null) {
		    player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED
			    + "You can't spawn this horse here! Try spawning it in the air.");
		    return;
		}

		horseLoader.loadHorseIntoWorld(player.getEyeLocation(), mount, player.getName());
		player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GREEN + "You have summoned your mount!");
		player.getInventory().removeItem(player.getItemInHand());
		player.updateInventory();
	    }
	}
    }

    @EventHandler
    public void onPigDamage(EntityDamageByEntityEvent event) {
	if (event.getDamager() instanceof Player && event.getEntity() instanceof Pig) {
	    Player p = (Player) event.getDamager();
	    if (p.getItemInHand().getType() == Material.SADDLE) {
		ItemStack saddle = p.getItemInHand();
		HorseMount mount = saddleHandler.getMountFromItem(saddle);

		if (mount != null) {
		    Pig pig = (Pig) event.getEntity();
		    pig.setSaddle(false);

		    p.setItemInHand(saddle);
		    event.setCancelled(true);
		}
	    }
	}
    }

    @EventHandler
    public void onPigClick(PlayerInteractEntityEvent event) {
	if (event.getPlayer().getItemInHand().getType() == Material.SADDLE) {
	    ItemStack saddle = event.getPlayer().getItemInHand();
	    HorseMount mount = saddleHandler.getMountFromItem(saddle);

	    if (mount != null) {
		event.setCancelled(true);

		event.getPlayer().setItemInHand(saddle);
	    }
	}
    }

    @EventHandler
    public void anvilUse(InventoryClickEvent e) {
	if (e.getClickedInventory() instanceof AnvilInventory) {
	    InventoryView view = e.getView();
	    for (int i = 0; i < 3; i++) {
		if (view.getItem(i) != null) {
		    ItemStack item = view.getItem(i);
		    if (item.hasItemMeta()) {
			HorseMount mount = saddleHandler.getMountFromItem(item);
			if (mount != null) {
			    e.getWhoClicked().sendMessage(
				    Commands.MOUNT_PREFIX + ChatColor.RED + "Don't put saddles in anvils ):");
			    e.setCancelled(true);
			}
		    }
		}
	    }
	}
    }
}
