package creeoer.plugins.mounts.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;
import creeoer.plugins.mounts.objects.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_15_R1.EntityHorse;


public class Commands implements CommandExecutor {
	
	private Mounts main;
	private static final Set<String> VALID_COMMANDS = new HashSet<>(Arrays.asList("rent", "give", "summon", "return", "reload", "remove"));
	public static final String MOUNT_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "Mounts" + ChatColor.DARK_GRAY + "]" + " ";
	private PlayerManager playerManager;
	private MountLoader mountLoader;
	
	public Commands(Mounts pluginInstance) {
		main = pluginInstance;
		playerManager = main.getPlayerManager();
		mountLoader = main.getMountLoader();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if(cmd.getLabel().equalsIgnoreCase("mount")) {
			
			if(args.length < 1) {
				sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "Too few arguments!");
				return false;
			}
			
			String command = args[0].toLowerCase();
			
			if(!VALID_COMMANDS.contains(command)) {
				sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "That is not a valid command!");
				return false;
			}
			
			
			if(command.equalsIgnoreCase("rent")) {
				
					if(args.length == 2) {
						
						if(sender.hasPermission("mounts.admin")) {
							Player player = Bukkit.getPlayer(args[1]);
							MountGUI gui = new MountGUI(main);
							player.openInventory(gui.getInventory());
							return false;
						}
					}
					
					if(sender instanceof Player) {
						Player player = (Player) sender;
					if(player.hasPermission("mounts.rent")) {
						MountGUI gui = new MountGUI(main);
						player.openInventory(gui.getInventory());
						
					} else {
						sender.sendMessage(MOUNT_PREFIX + ChatColor.GRAY + "You have no permission to rent a horse!");
						//no perms
						return false;
				 	  }			
					}
					
				}		
			 
			
			if(command.equalsIgnoreCase("reload")) {
				if(!sender.hasPermission("mounts.admin")) {
					sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You have no permission to do this.");
					return false;
				} 
				main.reloadConfigAndMounts();
				sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GREEN + "Config was successfully reloaded!");
				
			}
			
			if(command.equalsIgnoreCase("return")) {
				
				if(!(sender instanceof Player)) {
					sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "You must be a player to execute this command!");
					return false;
				}
				
				List<MountEntity> playerHorses = playerManager.getPlayerHorsesInWorld(sender.getName());
				
			
				if(playerHorses.isEmpty()) {
					sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You don't have a horse to return");
					return false;
				}
				
				MountEntity playerMount = null;
				HorseMount mountType = null;
				
				
				for(MountEntity mount: playerHorses) {	
				 playerMount = mount;
			     mountType = main.retrieveHorseMountType(mount.getUniqueID());
				
					if(mountType.isOwnable()) 
						break;
					
				}
	
				
				
				
				if(!mountType.isOwnable()) {
					sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "You can't return this horse as you are currently renting it");
					return false;
				}
				
				
				ItemStack saddle = mountLoader.getHorseMountAndSaddleMap().get(mountType);
				saddle.setType(Material.SADDLE);
				playerManager.removeHorseEntity(playerMount, sender.getName());
				
	
				((Player) sender).getInventory().addItem(saddle);
				 
				
			}
			
			if(command.equalsIgnoreCase("give")) {
				if(!sender.hasPermission("mounts.give")) {
					sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "You have no permission to execute this command!");
					return false;
				}
				
				if(args.length < 3) {
					sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "Not enough arguments!");  
					return false;
				}
				
				HorseMount mount = mountLoader.getMountFromID(args[1]);
				
				if(mount == null) {
					sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "This horse type does not exist!");
					return false;
				}
				
				
				String playerName = args[2];
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
				
				if(!offlinePlayer.isOnline()) {
					sender.sendMessage(MOUNT_PREFIX + ChatColor.RED + "The specified player is not online");
					return false;
				}
				
				Player player = Bukkit.getPlayer(args[2]);
				ItemStack stack = mountLoader.getHorseMountAndSaddleMap().get(mount);
				stack.setType(Material.SADDLE);
				
				player.getInventory().addItem(stack);
				sender.sendMessage(MOUNT_PREFIX + ChatColor.GREEN + "You've given player " + player.getName() + " a horse!");
				player.sendMessage(MOUNT_PREFIX + ChatColor.AQUA + "You have been given a horse saddle");
				 
				
			}
			
			if(command.equalsIgnoreCase("remove")) {
				if(sender.hasPermission("mounts.admin")) {
					
					if(args.length < 2) {
						sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.RED + "Not enough arguements!");
						return false;
					}
				
				String playerName = args[1];
				
				List<MountEntity> mounts = playerManager.getPlayerHorsesInWorld(sender.getName());
				
				for(MountEntity mount: mounts) {
					playerManager.removeHorseEntity(mount, playerName);
					sender.sendMessage(Commands.MOUNT_PREFIX + ChatColor.GRAY + "Player horses removed");
					}
				
				for (World world: Bukkit.getWorlds()) {
					for(Entity entity: world.getEntities()) {
						if(entity instanceof Horse) {
							EntityHorse horseEntity = ((CraftHorse) entity).getHandle();
						
								String horseName = horseEntity.getCustomName().getString();
								String senderName = sender.getName() + "'s"  + " Horse";
								
								if(horseName.equals(senderName)) 
									horseEntity.getBukkitEntity().remove();
							
						}
					}
				}
				
				}
			  
			}
			
			
			if(command.equalsIgnoreCase("summon")) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					
					List<MountEntity> mounts = playerManager.getPlayerHorsesInWorld(sender.getName());
					
					if(!mounts.isEmpty()) {
						player.sendMessage(MOUNT_PREFIX + ChatColor.GRAY + "Your horse was teleported to you.");
						
						for(MountEntity horse: mounts) {
							horse.getBukkitEntity().getLocation().getChunk().load();
							horse.getBukkitEntity().teleport(player.getLocation());
							return true;
						}
						
					} else {
						player.sendMessage(MOUNT_PREFIX + ChatColor.GRAY + "You have no horses to summon!");
					}
					
					
				}
				
			}
			
			
			
			
		}
		
		
		return false;
	}

}
