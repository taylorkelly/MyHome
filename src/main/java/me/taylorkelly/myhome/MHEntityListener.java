package me.taylorkelly.myhome;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.Plugin;

public class MHEntityListener extends EntityListener {
    private HomeList homeList;

    public MHEntityListener(HomeList homeList) {
        this.homeList = homeList;
    }

    public void onEntityDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            homeList.sendPlayerHome(player);
        }
    }

}
