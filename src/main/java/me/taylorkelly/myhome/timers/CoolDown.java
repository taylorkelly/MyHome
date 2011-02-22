package me.taylorkelly.myhome.timers;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import me.taylorkelly.myhome.HomeSettings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CoolDown {
    private static HashMap<String, Timer> players = new HashMap<String, Timer>();

    public static void addPlayer(Player player) {
        if (HomeSettings.coolDown > 0) {
            if (players.containsKey(player.getName())) {
                Timer timer = players.get(player.getName());
                timer.cancel();
            }

            Timer timer = new Timer();
            timer.schedule(new CoolTask(player), HomeSettings.coolDown*1000, HomeSettings.coolDown*1000);
            players.put(player.getName(), timer);
        }
    }

    public static boolean playerHasCooled(Player player) {
        return !players.containsKey(player.getName());
    }

    public static int timeLeft(Player player) {
        if (players.containsKey(player.getName())) {
            Timer timer = players.get(player.getName());
            // TODO
            return 0;
        } else {
            return 0;
        }
    }

    private static class CoolTask extends TimerTask {
        private Player player;

        public CoolTask(Player player) {
            this.player = player;
        }

        public void run() {
            if (HomeSettings.coolDownNotify)
                player.sendMessage(ChatColor.AQUA + "You have cooled down, feel free to /home");
            players.remove(player.getName());
            this.cancel();
        }
    }
}
