package creeoer.plugins.mounts.main;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import creeoer.plugins.mounts.objects.HorseMount;

public class MountGUI {
    /**
     * Get horse mounts, if armor is set to none put leather gui name Rent A Horse.
     */

    private Inventory inventory;

    private SaddleHandler saddleHandler;

    public MountGUI(Mounts pluginInstance) {
	inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Rent A Horse");
	saddleHandler = pluginInstance.getSaddleHandler();
	loadHorseButtons();
    }

    public Inventory getInventory() {
	return inventory;
    }

    /** Load horse buttons. */
    public void loadHorseButtons() {
	// loop through set of horse mounts, get itemstack and place in vneotyry
	int currentSlot = 11;
	for (Entry<HorseMount, ItemStack> entry : saddleHandler.getHorseMountAndSaddleMap().entrySet()) {
	    if (!entry.getKey().isOwnable()) {
		ItemStack stack = entry.getValue();
		stack.setType(Material.SADDLE);

		inventory.setItem(currentSlot, stack);

		currentSlot += 2;
	    }
	}
    }
}
