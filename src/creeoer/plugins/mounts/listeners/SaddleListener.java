package creeoer.plugins.mounts.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.main.Commands;
import creeoer.plugins.mounts.main.MountLoader;
import creeoer.plugins.mounts.main.Mounts;
import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.objects.PlayerManager;
import creeoer.plugins.mounts.objects.ReturnHorseTask;
import net.md_5.bungee.api.ChatColor;

public class SaddleListener implements Listener {
	
	private MountLoader mountLoader;
	private PlayerManager playerManager;
	private ReturnHorseTask task;
	private Mounts main;
	
	public SaddleListener(Mounts main) {
		mountLoader = main.getMountLoader();
		playerManager = main.getPlayerManager();
		this.main = main;
	}
	
	
	

	@EventHandler
	public void onSaddleClick(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
			if(event.getPlayer().getItemInHand().getType() == Material.SADDLE) {
				if(event.getPlayer().getItemInHand().hasItemMeta()) {
					HorseMount mount = mountLoader.getMountFromItem(event.getPlayer().getItemInHand());

					if(mount != null) {
						Player player = event.getPlayer();
						event.setCancelled(true);
						
						if(playerManager.isRenting(player.getName())) {
							player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You can't spawn your owned horse since you are currently renting a horse");
							return;
						}
						
						if(!playerManager.getPlayerHorsesInWorld(player.getName()).isEmpty()) {
							player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You can't spawn another horse with one summoned!");
							return;
						}
						
						if(event.getClickedBlock() != null) {
							player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You can't spawn this horse here! Try spawning it in the air.");
							return;
						}
				
						   Horse horse = MountEntity.spawn(event.getPlayer().getEyeLocation(), mount.getSpeed(), player.getName(), mount);
						   horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
						   horse.getInventory().setArmor(mount.getArmor());
						   horse.setVariant(mount.getHorseType());
						   
						   
						   if(horse.getVariant() != Variant.UNDEAD_HORSE || horse.getVariant() != Variant.SKELETON_HORSE)
						   horse.setColor(mount.getColor());
						   
						   player.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GREEN + "You have summoned your mount!");
						   main.registerHorseInWorld(horse.getUniqueId(), mount);
						   player.getInventory().removeItem(player.getItemInHand());
						   player.updateInventory();
					}
				}	
			}	
		}
				
	}
	
	
	@EventHandler
	public void onPigDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Pig) {
			Player p = (Player) event.getDamager();
			if(p.getItemInHand().getType() == Material.SADDLE) {
				ItemStack saddle = p.getItemInHand();
				HorseMount mount = mountLoader.getMountFromItem(saddle);
				
				if(mount != null) {
					
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
		if(event.getPlayer().getItemInHand().getType() == Material.SADDLE) {
			ItemStack saddle = event.getPlayer().getItemInHand();
			HorseMount mount = mountLoader.getMountFromItem(saddle);
			
			if(mount != null) {
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
	                        HorseMount mount = mountLoader.getMountFromItem(item);
	                        if (mount != null) {
	                   
	                                e.getWhoClicked().sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "Don't put saddles in anvils ):");
	                                e.setCancelled(true);
	                            
	   
	                        }
	                    }
	                }

	            }
	        }
	}
	
	
}
