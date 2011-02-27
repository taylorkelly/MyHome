package me.taylorkelly.myhome;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class HomePermissions {
    private static Permissions permissionsPlugin;
    private static boolean permissionsEnabled = false;

    public static void initialize(Server server) {
        Plugin test = server.getPluginManager().getPlugin("Permissions");
        if (test != null) {
            Logger log = Logger.getLogger("Minecraft");
            permissionsPlugin = ((Permissions)test);
            permissionsEnabled = true;
            log.log(Level.INFO, "[MYHOME] Permissions enabled.");
        } else {
            Logger log = Logger.getLogger("Minecraft");
            log.log(Level.SEVERE, "[MYHOME] Permissions isn't loaded, there are no restrictions.");
        }
    }

    public static boolean isAdmin(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.admin");
        } else {
            return player.isOp();
        }
    }

    private static boolean permission(Player player, String string) {
        return permissionsPlugin.Security.permission(player, string);
    }

    public static boolean home(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.basic.home");
        } else {
            return true;
        }
    }

    public static boolean set(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.basic.set");
        } else {
            return true;
        }
    }

    public static boolean delete(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.basic.delete");
        } else {
            return true;
        }
    }

    public static boolean list(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.list");
        } else {
            return true;
        }
    }

    public static boolean homeOthers(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.others");
        } else {
            return true;
        }
    }

    public static boolean invite(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.invite");
        } else {
            return true;
        }
    }

    public static boolean uninvite(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.uninvite");
        } else {
            return true;
        }
    }

    public static boolean canPublic(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.public");
        } else {
            return true;
        }
    }

    public static boolean canPrivate(Player player) {
        if (permissionsEnabled) {
            return permission(player, "myhome.home.soc.private");
        } else {
            return true;
        }
    }
}
