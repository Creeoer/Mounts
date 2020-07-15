package creeoer.plugins.mounts.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.main.UnrentGUI;
import creeoer.plugins.mounts.mysql.PlayerManager;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.tasks.ReturnHorseTask;
import net.minecraft.server.v1_15_R1.EntityHorse;

public class HorseListener implements Listener {
    private PlayerManager playerManager;
    private ReturnHorseTask task;
    private Mounts main;

    public HorseListener(Mounts main) {
	this.main = main;
	playerManager = main.getPlayerManager();
    }

    @EventHandler
    public void onHorseRide(VehicleEnterEvent event) {
	if (event.getVehicle() instanceof Horse) {
	    CraftHorse craftHorse = (CraftHorse) event.getVehicle();
	    EntityHorse horse = craftHorse.getHandle();
	    if (horse instanceof MountEntity && event.getEntered() instanceof Player) {
		String horseName = horse.getCustomName().getString();
		String playerName = ((Player) event.getEntered()).getName();

		if (!horseName.contains(playerName)) {
		    event.setCancelled(true);
		    ((Player) event.getEntered())
			    .sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "This is not your horse!");
		    return;
		}

		if (task != null) {
		    task.cancel();
		    task = null;
		}
	    }
	}
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
	if (event.getEntity() instanceof Player && event.getEntity().getVehicle() != null
		&& event.getEntity().getVehicle() instanceof Horse) {
	    event.getEntity().getVehicle().eject();
	}
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
	for (Entity entity : event.getChunk().getEntities()) {
	    if (entity instanceof Horse) {
		EntityHorse horse = ((CraftHorse) entity).getHandle();
		if (horse instanceof MountEntity) {
		    UUID horseID = horse.getUniqueID();
		    if (playerManager.getHorsesToBeDeleted().contains(horseID)) {
			entity.remove();
			playerManager.removeHorseFromQueue(horseID);
		    }
		}
	    }
	}
    }

    @EventHandler
    public void onHorseLeave(VehicleExitEvent event) {
	if (event.getVehicle() instanceof Horse) {
	    EntityHorse horse = ((CraftHorse) event.getVehicle()).getHandle();
	    if (horse instanceof MountEntity) {
		MountEntity entity = (MountEntity) horse;

		HorseMount mountType = main.retrieveHorseMountType(entity.getUniqueID());

		Player p = (Player) event.getExited();

		if (mountType.isOwnable() && !event.isCancelled() && p.isOnline()) {
		    task = new ReturnHorseTask(main, entity);
		    task.runTaskLater(main, 80L);
		}
	    }
	}
    }

    @EventHandler
    public void onHorseInventoryOpen(InventoryOpenEvent event) {
	if (event.getInventory().getHolder() instanceof Horse) {
	    event.setCancelled(true);
	}
    }

    @EventHandler
    public void horseDamageEvent(EntityDamageEvent event) {
	if (event.getEntity() instanceof Horse) {
	    EntityHorse horse = ((CraftHorse) event.getEntity()).getHandle();
	    if (horse instanceof MountEntity) {
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler
    public void playerDamageHorse(EntityDamageByEntityEvent event) {
	if (event.getEntity() instanceof Horse) {
	    EntityHorse horse = ((CraftHorse) event.getEntity()).getHandle();
	    if (horse instanceof MountEntity) {
		event.setCancelled(true);
		if (event.getDamager() instanceof Player) {
		    Player player = (Player) event.getDamager();
		    if (playerManager.getPlayerHorseID(player.getName()) != null) {
			MountEntity entity = (MountEntity) horse;
			HorseMount mountType = main.retrieveHorseMountType(entity.getUniqueID());
			if (!mountType.isOwnable()) {
			    UnrentGUI unrent = new UnrentGUI(entity);
			    player.openInventory(unrent.getInventory());
			}
		    }
		}
	    }
	}
    }
}
