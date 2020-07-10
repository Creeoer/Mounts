package creeoer.plugins.mounts.main;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;

import creeoer.plugins.mounts.objects.MountEntity;
import net.minecraft.server.v1_15_R1.EntityHorse;



public class HorseUtils {
	
	
	@SuppressWarnings("deprecation")
	public static Variant parseVariantFromString(String variant) {
		
		switch(variant.toLowerCase()) {
		
		case "undead":
			return Variant.UNDEAD_HORSE;
		case "donkey":
			return Variant.DONKEY;
		case "mule":
			return Variant.MULE;
		case "skeleton":
			return Variant.SKELETON_HORSE;
		default:
			return Variant.HORSE;
		
		}
		
	}
	
	public static Horse.Color parseColorFromString(String color) {
		
		switch(color.toLowerCase()) {
		
		case "orange":
			return Horse.Color.WHITE;
		
		case "brown":
			return Horse.Color.BROWN;
		case "black":
			return Horse.Color.BLACK;
		case "chestnut":
			return Horse.Color.CHESTNUT;
		case "creamy":
			return Horse.Color.CREAMY;
		case "white":
			return Horse.Color.WHITE;
		case "gray":
			return Horse.Color.GRAY;
			
		default:
			return Horse.Color.WHITE;
		}
	}
	
	public static Material parseArmorFromString(String armorName) {
		
		switch(armorName.toLowerCase()) {
		
		case "none":
			return Material.AIR;
		case "iron":
			return Material.LEGACY_IRON_BARDING;
		case "gold":
			return Material.LEGACY_GOLD_BARDING;
		case "diamond":
			return Material.LEGACY_DIAMOND_BARDING;
		
		default:
			return Material.AIR;
	
		
		}
		
		
	}
	
	
	public static String getHorseType(Variant horseType) {
		
		switch(horseType) {
		
		case UNDEAD_HORSE:
			return "Zombie";
		case SKELETON_HORSE:
			return "Skeleton";
		
		default:
			return horseType.name();
		
		}
		
	}

	public static String getMountType(Horse.Color color, Variant horseType) {
		
		switch(horseType) {
		
		case SKELETON_HORSE:
		case UNDEAD_HORSE:
			return "Unique";
			
		default:
			return color.toString();
		
		}
	
	}
	
	public static MountEntity getHorseFromUUID(UUID horseID) {
		for (World world: Bukkit.getWorlds()) {
			for(Entity entity: world.getEntities()) {
				if(entity instanceof Horse) {
					EntityHorse horse = ((CraftHorse) entity).getHandle();
					if(horse instanceof MountEntity) {

					if(horse.getUniqueID().equals(horseID))
						return (MountEntity) horse;
					
					}
				}
			  }
			}
		return null;
	
	}

}
