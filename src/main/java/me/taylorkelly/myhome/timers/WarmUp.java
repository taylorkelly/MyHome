package me.taylorkelly.myhome.timers;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import me.taylorkelly.myhome.Home;
import me.taylorkelly.myhome.HomeSettings;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class WarmUp {
    private static HashMap<String, Timer> players = new HashMap<String, Timer>();

    public static void addPlayer(Player player, Home home, Server server) {
        if (HomeSettings.coolDown > 0) {
            if (players.containsKey(player.getName())) {
                Timer timer = players.get(player.getName());
                timer.cancel();
            }
            if (HomeSettings.warmUpNotify && HomeSettings.warmUp > 0) {
                player.sendMessage(ChatColor.RED + "You will have to warm up for " + HomeSettings.warmUp + " secs");
            }
            Timer timer = new Timer();
            timer.schedule(new WarmTask(player, home, server), HomeSettings.warmUp*1000, HomeSettings.warmUp*1000);
            players.put(player.getName(), timer);
        } else {
            home.warp(player, server);
        }
    }

    public static boolean playerHasWarmed(Player player) {
        return players.containsKey(player.getName());
    }

    private static void sendPlayer(Player player, Home home, Server server) {
        if (HomeSettings.warmUpNotify && HomeSettings.warmUp > 0)
            player.sendMessage(ChatColor.RED + "You have warmed up! Sending you /home");
        home.warp(player, server);
    }

    private static class WarmTask extends TimerTask {
        private Player player;
        private Home home;
        private Server server;

        public WarmTask(Player player, Home home, Server server) {
            this.player = player;
            this.home = home;
            this.server = server;
        }

        public void run() {
            sendPlayer(player, home, server);
            players.remove(player.getName());
            this.cancel();
        }
    }
}
