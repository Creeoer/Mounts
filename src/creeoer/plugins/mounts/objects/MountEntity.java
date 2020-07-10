package creeoer.plugins.mounts.objects;


import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;

import org.bukkit.entity.Horse;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityHorse;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.World;



public class MountEntity extends EntityHorse{
	
	private HorseMount mountType;
	
	
	
	public MountEntity(World world) {
	    super(EntityTypes.HORSE, world);
	}
	
	//This will contain the horse entity itself, the speed, as well as the owner's name

	
	public MountEntity (World world, int speed, String renterName, HorseMount mountType) {
		super(EntityTypes.HORSE, world);
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.22499999403953552D + (double) (speed / 10) + (double) .2);
		setCustomName(new ChatMessage(renterName + "'s" + " " + "Horse"));
		setCustomNameVisible(true);
		setTamed(true);
		
		//setOwnerUUID(Bukkit.getOfflinePlayer(renterName).getUniqueId().toString());
		this.mountType = mountType;
		this.persistent = true;
	
	
		
		
	}
	
	
    public static Horse spawn(Location loc, int speed, String renterName, HorseMount mountType){
    	
        World mcWorld = ((CraftWorld) loc.getWorld()).getHandle();
        MountEntity customEnt = new MountEntity(mcWorld, speed, renterName, mountType);
        customEnt.setMountType(mountType);
        customEnt.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        ((CraftLivingEntity) customEnt.getBukkitEntity()).setRemoveWhenFarAway(false); 
        mcWorld.addEntity(customEnt, SpawnReason.CUSTOM);
        return (Horse) customEnt.getBukkitEntity();
        
    }
    
    public void setMountType(HorseMount mountType) {
    	this.mountType = mountType;
    }
    
    public HorseMount getMountType() {
    	return mountType;
    }

}
