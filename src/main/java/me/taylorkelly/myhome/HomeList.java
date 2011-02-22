package me.taylorkelly.myhome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.taylorkelly.myhome.timers.CoolDown;
import me.taylorkelly.myhome.timers.WarmUp;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class HomeList {

    private HashMap<String, Home> homeList;
    private Server server;

    public HomeList(Server server) {
        this.server = server;
        WarpDataSource.initialize();
        homeList = WarpDataSource.getMap();
    }

    public void addHome(Player player) {
        if (homeList.containsKey(player.getName())) {
            Home warp = homeList.get(player.getName());
            warp.setLocation(player.getLocation());
            WarpDataSource.moveWarp(warp);
            player.sendMessage(ChatColor.AQUA + "Welcome to your new home :).");
        } else {
            Home warp = new Home(player);
            homeList.put(player.getName(), warp);
            WarpDataSource.addWarp(warp);
            player.sendMessage(ChatColor.AQUA + "Successfully created your home");
            if (HomePermissions.invite(player)) {
                player.sendMessage("If you'd like to invite friends to it,");
                player.sendMessage("Use: " + ChatColor.RED + "/home invite <player>");
            }
        }
        MyHome.setCompass(player, player.getLocation());
    }

    public void blindAdd(Home warp) {
        homeList.put(warp.name, warp);
    }

    public void warpTo(String name, Player player) {
        MatchList matches = this.getMatches(name, player);
        name = matches.getMatch(name);
        if (homeList.containsKey(name)) {
            Home warp = homeList.get(name);
            if (warp.playerCanWarp(player)) {
                if (CoolDown.playerHasCooled(player)) {
                    WarmUp.addPlayer(player, warp, server);
                    CoolDown.addPlayer(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You need to wait for the cooldown of " + HomeSettings.coolDown + " secs");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to warp to " + name + "'s home");
            }
        } else {
            player.sendMessage(ChatColor.RED + name + " doesn't have a home :(");
        }
    }

    public void sendPlayerHome(Player player) {
        if (homeList.containsKey(player.getName())) {
            if (CoolDown.playerHasCooled(player)) {
                WarmUp.addPlayer(player, homeList.get(player.getName()), server);
                CoolDown.addPlayer(player);
            } else {
                player.sendMessage(ChatColor.RED + "You need to wait for the cooldown of " + HomeSettings.coolDown + " secs");
            }
        }
    }

    public boolean playerHasHome(Player player) {
        return homeList.containsKey(player.getName());
    }

    public void deleteHome(Player player) {
        if (homeList.containsKey(player.getName())) {
            Home warp = homeList.get(player.getName());
            homeList.remove(player.getName());
            WarpDataSource.deleteWarp(warp);
            player.sendMessage(ChatColor.AQUA + "You have deleted your home");
        } else {
            player.sendMessage(ChatColor.RED + "You have no home to delete :(");
        }
    }

    public void privatize(Player player) {
        if (homeList.containsKey(player.getName())) {
            Home warp = homeList.get(player.getName());
            warp.publicAll = false;
            WarpDataSource.publicizeWarp(warp, false);
            player.sendMessage(ChatColor.AQUA + "You have privatized your home");
            if (HomePermissions.invite(player)) {
                player.sendMessage("If you'd like to invite others to it,");
                player.sendMessage("Use: " + ChatColor.RED + "/home invite <player>");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You have no home to privatize :(");
        }
    }

    public void publicize(Player player) {
        if (homeList.containsKey(player.getName())) {
            Home warp = homeList.get(player.getName());
            warp.publicAll = true;
            WarpDataSource.publicizeWarp(warp, true);
            player.sendMessage(ChatColor.AQUA + "You have publicized your home.");
        } else {
            player.sendMessage(ChatColor.RED + "You have no home to publicize :(");
        }
    }

    public void invite(Player player, String inviteeName) {
        if (homeList.containsKey(player.getName())) {
            // TODO match player stuff
            Home warp = homeList.get(player.getName());
            if (warp.playerIsInvited(inviteeName)) {
                player.sendMessage(ChatColor.RED + inviteeName + " is already invited to your home.");
            } else if (warp.playerIsCreator(inviteeName)) {
                player.sendMessage(ChatColor.RED + "This is your home!");
            } else {
                warp.invite(inviteeName);
                WarpDataSource.updatePermissions(warp);
                player.sendMessage(ChatColor.AQUA + "You have invited " + inviteeName + " to your home");
                if (warp.publicAll) {
                    player.sendMessage(ChatColor.RED + "But your home is still public!");
                }
                Player match = server.getPlayer(inviteeName);
                if (match != null) {
                    match.sendMessage(ChatColor.AQUA + "You've been invited to " + player.getName() + "'s home");
                    match.sendMessage("Use: " + ChatColor.RED + "/home " + player.getName() + ChatColor.WHITE + " to warp to it.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You have no home to invite people to :(");
        }
    }

    public void uninvite(Player player, String inviteeName) {
        if (homeList.containsKey(player.getName())) {
            // TODO player match stuff
            Home warp = homeList.get(player.getName());
            if (!warp.playerIsInvited(inviteeName)) {
                player.sendMessage(ChatColor.RED + inviteeName + " is not invited to your home.");
            } else if (warp.playerIsCreator(inviteeName)) {
                player.sendMessage(ChatColor.RED + "Why would you want to uninivite yourself?");
            } else {
                warp.uninvite(inviteeName);
                WarpDataSource.updatePermissions(warp);
                player.sendMessage(ChatColor.AQUA + "You have uninvited " + inviteeName + " from your home");
                if (warp.publicAll) {
                    player.sendMessage(ChatColor.RED + "But your home is still public.");
                }
                Player match = server.getPlayer(inviteeName);
                if (match != null) {
                    match.sendMessage(ChatColor.RED + "You've been uninvited to " + player.getName() + "'s home. Sorry.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You have no home to uninvite people from :(");
        }
    }

    public boolean homeExists(String name) {
        return homeList.containsKey(name);
    }

    public void list(Player player) {
        ArrayList<Home> results = homesInvitedTo(player);

        if (results.size() == 0) {
            player.sendMessage(ChatColor.RED + "You are invited to no one's home.");
        } else {
            player.sendMessage(ChatColor.AQUA + "You are invited to the homes of:");
            player.sendMessage(results.toString().replace("[", "").replace("]", ""));
        }
    }

    public void ilist(Player player) {
        if (homeList.containsKey(player.getName())) {
            Home warp = homeList.get(player.getName());
            if (warp.permissions.size() == 0) {
                player.sendMessage(ChatColor.AQUA + "No one is invited to your house");
            } else {
                player.sendMessage(ChatColor.AQUA + warp.invitees() + " invited to your house");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You have no home :(");
        }
    }

    private ArrayList<Home> homesInvitedTo(Player player) {
        ArrayList<Home> results = new ArrayList<Home>();
        for (Home home : homeList.values()) {
            if (home.playerCanWarp(player) && !home.playerIsCreator(player.getName())) {
                results.add(home);
            }
        }
        return results;
    }

    public void orientPlayer(Player player) {
        if (playerHasHome(player)) {
            Home home = homeList.get(player.getName());
            World world = player.getWorld();
            Location location = new Location(world, home.x, home.y, home.z);
            MyHome.setCompass(player, location);
        }
    }

    public MatchList getMatches(String name, Player player) {
        ArrayList<Home> exactMatches = new ArrayList<Home>();
        ArrayList<Home> matches = new ArrayList<Home>();

        List<String> names = new ArrayList<String>(homeList.keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (int i = 0; i < names.size(); i++) {
            String currName = names.get(i);
            Home warp = homeList.get(currName);
            if (warp.playerCanWarp(player)) {
                if (warp.name.equalsIgnoreCase(name)) {
                    exactMatches.add(warp);
                } else if (warp.name.toLowerCase().contains(name.toLowerCase())) {
                    matches.add(warp);
                }
            }
        }
        if (exactMatches.size() > 1) {
            for (Home warp : exactMatches) {
                if (!warp.name.equals(name)) {
                    exactMatches.remove(warp);
                    matches.add(0, warp);
                }
            }
        }
        return new MatchList(exactMatches, matches);
    }

    public Home getHomeFor(Player player) {
        return homeList.get(player.getName());
    }
}

class MatchList {

    public MatchList(ArrayList<Home> exactMatches, ArrayList<Home> matches) {
        this.exactMatches = exactMatches;
        this.matches = matches;
    }
    public ArrayList<Home> exactMatches;
    public ArrayList<Home> matches;

    public String getMatch(String name) {
        if (exactMatches.size() == 1) {
            return exactMatches.get(0).name;
        }
        if (exactMatches.size() == 0 && matches.size() == 1) {
            return matches.get(0).name;
        }
        return name;
    }
}
