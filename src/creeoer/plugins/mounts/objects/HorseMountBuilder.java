package creeoer.plugins.mounts.objects;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

public class HorseMountBuilder {
    /** Builds horse mount. */

    private String id;
    private String name;
    private int speed;
    private double price;
    private long rentTime;
    private ItemStack armor;
    private Variant horseType;
    private boolean isOwnable;
    private Horse.Color color;

    public HorseMountBuilder(String name, int speed, String id) {
	this.name = name;
	this.speed = speed;
	this.id = id;
    }

    public HorseMountBuilder setPrice(double price) {
	this.price = price;
	return this;
    }

    public HorseMountBuilder setColor(Horse.Color color) {
	this.color = color;
	return this;
    }

    public HorseMountBuilder setRentTime(long timeInTicks) {
	this.rentTime = timeInTicks;
	return this;
    }

    public HorseMountBuilder setArmorType(ItemStack armor) {
	this.armor = armor;
	return this;
    }

    public HorseMountBuilder setOwnable(boolean isOwnable) {
	this.isOwnable = isOwnable;
	return this;
    }

    public HorseMountBuilder setHorseType(Variant horseType) {
	this.horseType = horseType;
	return this;
    }

    public HorseMount build() {
	return new HorseMount(name, speed, id, price, rentTime, armor, isOwnable, color);
    }
}
