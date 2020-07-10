package creeoer.plugins.mounts.objects;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.ItemStack;

public class HorseMount {
	
	
	private String id;
	private String name;
	private int speed;
	private double price;
	private long rentTime;
	private ItemStack armor;
	private boolean isOwnable;
	private Variant horseType;
	private Horse.Color color;
	
	
	protected HorseMount(String name, int speed, String id,  double price, long rentTime, ItemStack armor, Variant horseType, boolean isOwnable, Horse.Color color) {
		this.id = id;
		this.name = name;
		this.speed = speed;
		this.price = price;
		this.rentTime = rentTime;
		this.armor = armor;
		this.horseType = horseType;
		this.isOwnable = isOwnable;
		this.color = color;
	}


	public ItemStack getArmor() {
		return armor;
	}


	public double getPrice() {
		return price;
	}




	public int getSpeed() {
		return speed;
	}

	public String getID() {
		return id;
	}


	public long getRentTime() {
		return rentTime;
	}



	public String getName() {
		return name;
	}

	public Variant getHorseType() {
		return horseType;
	}
	
	public boolean isOwnable() {
		return isOwnable;
	}
	
	public Horse.Color getColor() {
		return color;
	}

	

}
