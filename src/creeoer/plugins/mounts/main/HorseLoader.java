package creeoer.plugins.mounts.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.objects.HorseMount;
import creeoer.plugins.mounts.objects.MountEntity;

public class HorseLoader {
    private Mounts main;

    public HorseLoader(Mounts main) {
	this.main = main;
    }

    public void loadHorseIntoWorld(Location loc, HorseMount mount, String ownerName) {
	loc.getChunk().load();
	Horse horse = MountEntity.spawn(loc, mount.getSpeed(), ownerName, mount);

	horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
	horse.getInventory().setArmor(mount.getArmor());
	horse.setColor(mount.getColor());

	main.registerHorseInWorld(horse.getUniqueId(), mount);
    }
}
