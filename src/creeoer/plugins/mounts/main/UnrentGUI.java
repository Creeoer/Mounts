package creeoer.plugins.mounts.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import creeoer.plugins.mounts.objects.MountEntity;
import net.md_5.bungee.api.ChatColor;

public class UnrentGUI {
    private Inventory inventory;
    private MountEntity mountEntity;

    public UnrentGUI(MountEntity mountEntity) {
	inventory = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Unrent Your Horse");
	loadButtons();
	this.mountEntity = mountEntity;
    }

    private void loadButtons() {
	ItemStack yesButton = new ItemStack(Material.POISONOUS_POTATO);
	ItemMeta yesButtonMeta = yesButton.getItemMeta();
	yesButtonMeta.setDisplayName(ChatColor.GREEN + "YES");
	yesButton.setItemMeta(yesButtonMeta);

	ItemStack noButton = new ItemStack(Material.BARRIER);
	ItemMeta noButtonMeta = noButton.getItemMeta();
	noButtonMeta.setDisplayName(ChatColor.RED + "NO");
	noButton.setItemMeta(noButtonMeta);

	inventory.setItem(2, noButton);
	inventory.setItem(6, yesButton);
    }

    public Inventory getInventory() {
	return inventory;
    }
}
