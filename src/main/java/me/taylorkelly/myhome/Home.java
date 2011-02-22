package me.taylorkelly.myhome;

import java.util.ArrayList;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class Home {
	public int index;
	public String name;
	public int world;
	public double x;
	public int y;
	public double z;
	public int yaw;
	public int pitch;
	public boolean publicAll;
	public String welcomeMessage;
	public ArrayList<String> permissions;
	
	public static int nextIndex = 1;
	
	public Home(int index, String name, int world, double x, int y, double z, int yaw, int pitch, boolean publicAll, String permissions, String welcomeMessage) {
		this.index = index;
		this.name = name;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.publicAll = publicAll;
		this.permissions = processList(permissions);
		this.welcomeMessage = welcomeMessage;
		if(index > nextIndex) nextIndex = index;
		nextIndex++;
	}
	
	public Home(Player creator) {
		this.index = nextIndex;
		nextIndex++;
		this.name = creator.getName();
		//TODO better world handling
		this.world = 0;
		this.x = creator.getLocation().getX();
		this.y = creator.getLocation().getBlockY();
		this.z = creator.getLocation().getZ();
		this.yaw = Math.round(creator.getLocation().getYaw()) % 360;
		this.pitch = Math.round(creator.getLocation().getPitch()) %360;
		this.publicAll = false;
		this.permissions = new ArrayList<String>();
		this.welcomeMessage = "Welcome to " + name + "'s home";
	}
	
	public Home(String name, Location location) {
		this.index = nextIndex;
		nextIndex++;
		this.name = name;
		//TODO better world handling
		this.world = 0;
		this.x = location.getX();
		this.y = location.getBlockY();
		this.z = location.getZ();
		this.yaw = Math.round(location.getYaw()) % 360;
		this.pitch = Math.round(location.getPitch()) % 360;
		this.publicAll = false;
		this.permissions = new ArrayList<String>();
		this.welcomeMessage = "Welcome to " + name + "'s home";
	}
	

	private ArrayList<String> processList(String permissions) {
		String[] names = permissions.split(",");
		ArrayList<String> ret = new ArrayList<String>();
		for(String name: names) {
			if(name.equals("")) continue;
			ret.add(name.trim());
		}
		return ret;
	}
	
	public String permissionsString() {
		StringBuilder ret = new StringBuilder();
		for(String name: permissions) {
			ret.append(name);
			ret.append(",");
		}
		return ret.toString();
	}

	public boolean playerCanWarp(Player player) {
		if(name.equals(player.getName())) return true;
		if(permissions.contains(player.getName())) return true;
	    if(HomePermissions.isAdmin(player)) return true;
		return publicAll;
	}

	public void warp(Player player) {
		//Better world support
		World world = player.getWorld();
		Location location = new Location(world, x, y, z, yaw, pitch);
		player.teleportTo(location);
        player.sendMessage(ChatColor.AQUA + this.welcomeMessage);
	}

	public boolean playerIsCreator(String player) {
		if(name.equals(player)) return true;
		return false;
	}
	
	public void invite(String player) {
		permissions.add(player);
	}
	
	public boolean playerIsInvited(String player) {
		return permissions.contains(player);
	}

	public void uninvite(String inviteeName) {
		permissions.remove(inviteeName);
	}

    public String invitees() {
        if(permissions.size() == 1) {
            return permissions.get(0) + " is";
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < permissions.size(); i++) {
            builder.append(permissions.get(i));
            if(i + 2 < permissions.size()) {
                builder.append(", ");
            } else if (i + 1 < permissions.size()){
                builder.append(" and");
            }
        }
        builder.append(" are ");
        return builder.toString();
    }
    
    public String toString() {
        return name;
    }

    public void setLocation(Location location) {
        this.x = location.getX();
        this.y = location.getBlockY();
        this.z = location.getZ();
    }
}
