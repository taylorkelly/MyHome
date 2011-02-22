package me.taylorkelly.myhome;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

public class MHPlayerListener extends PlayerListener{
    private HomeList homeList;
    
    public MHPlayerListener(HomeList homeList) {
        this.homeList = homeList;
    }
    public void onPlayerJoin(PlayerEvent event) {
        if(homeList.homeExists(event.getPlayer().getName())) {
            homeList.orientPlayer(event.getPlayer());
        }        
    }
    
}
