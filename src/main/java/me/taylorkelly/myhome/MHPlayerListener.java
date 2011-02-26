package me.taylorkelly.myhome;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class MHPlayerListener extends PlayerListener {

    private HomeList homeList;
    private Server server;

    public MHPlayerListener(HomeList homeList, Server server) {
        this.homeList = homeList;
        this.server = server;
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        if (homeList.homeExists(event.getPlayer().getName())) {
            homeList.orientPlayer(event.getPlayer());
        }
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (HomeSettings.respawnToHome && homeList.homeExists(event.getPlayer().getName())) {
            Location location = homeList.getHomeFor(event.getPlayer()).getLocation(server);
            if (location != null) {
                event.setRespawnLocation(location);
                homeList.orientPlayer(event.getPlayer());
            }
        }
    }
}
