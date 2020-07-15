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
    public static Variant parseVariantFromString(String variant) {
	switch (variant.toLowerCase()) {

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
	switch (color.toLowerCase()) {

	case "orange":
	case "white":
	default:
	    return Horse.Color.WHITE;

	case "brown":
	    return Horse.Color.BROWN;
	case "black":
	    return Horse.Color.BLACK;
	case "chestnut":
	    return Horse.Color.CHESTNUT;
	case "creamy":
	    return Horse.Color.CREAMY;
	case "gray":
	    return Horse.Color.GRAY;
	}
    }

    public static Material parseArmorFromString(String armorName) {
	switch (armorName.toLowerCase()) {

	case "none":
	default:
	    return Material.AIR;
	case "iron":
	    return Material.IRON_HORSE_ARMOR;
	case "gold":
	    return Material.GOLDEN_HORSE_ARMOR;
	case "diamond":
	    return Material.DIAMOND_HORSE_ARMOR;

	}
    }

    public static String getHorseType(Variant horseType) {
	switch (horseType) {

	case UNDEAD_HORSE:
	    return "Zombie";
	case SKELETON_HORSE:
	    return "Skeleton";

	default:
	    return horseType.name();

	}
    }

    public static String getMountType(Horse.Color color, Variant horseType) {
	switch (horseType) {

	case SKELETON_HORSE:
	case UNDEAD_HORSE:
	    return "Unique";

	default:
	    return color.toString();

	}
    }

    public static MountEntity getHorseFromUUID(UUID horseID) {
	for (World world : Bukkit.getWorlds()) {
	    for (Entity entity : world.getEntities()) {
		if (entity instanceof Horse) {
		    EntityHorse horse = ((CraftHorse) entity).getHandle();
		    if (horse instanceof MountEntity && horse.getUniqueID().equals(horseID)) {
			return (MountEntity) horse;
		    }
		}
	    }
	}
	return null;
    }
}
