package me.taylorkelly.myhome;

import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.anjocaido.groupmanager.GroupManager;

public class HomePermissions {

    private enum PermissionHandler {

        PERMISSIONS, GROUP_MANAGER, NONE
    }
    private static PermissionHandler handler;
    private static Plugin permissionPlugin;

    public static void initialize(Server server) {
        Plugin groupManager = server.getPluginManager().getPlugin("GroupManager");
        Plugin permissions = server.getPluginManager().getPlugin("Permissions");

        if (groupManager != null) {
            permissionPlugin = groupManager;
            handler = PermissionHandler.GROUP_MANAGER;
            String version = groupManager.getDescription().getVersion();
            HomeLogger.info("Permissions enabled using: GroupManager v" + version);
        } else if (permissions != null) {
            permissionPlugin = permissions;
            handler = PermissionHandler.PERMISSIONS;
            String version = permissions.getDescription().getVersion();
            HomeLogger.info("Permissions enabled using: Permissions v" + version);
        } else {
            handler = PermissionHandler.NONE;
            HomeLogger.warning("A permission plugin isn't loaded.");
        }
    }

    public static boolean permission(Player player, String permission, boolean defaultPerm) {
        switch (handler) {
            case PERMISSIONS:
                return ((Permissions) permissionPlugin).getHandler().has(player, permission);
            case GROUP_MANAGER:
                return ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, permission);
            case NONE:
                return defaultPerm;
            default:
                return defaultPerm;
        }
    }

    public static boolean isAdmin(Player player) {
        return permission(player, "myhome.admin", player.isOp());
    }

    public static boolean home(Player player) {
        return permission(player, "myhome.home.basic.home", true);
    }

    public static boolean set(Player player) {
        return permission(player, "myhome.home.basic.set", true);
    }

    public static boolean delete(Player player) {
        return permission(player, "myhome.home.basic.delete", true);
    }

    public static boolean list(Player player) {
        return permission(player, "myhome.home.soc.list", true);
    }

    public static boolean homeOthers(Player player) {
        return permission(player, "myhome.home.soc.others", true);
    }

    public static boolean invite(Player player) {
        return permission(player, "myhome.home.soc.invite", true);
    }

    public static boolean uninvite(Player player) {
        return permission(player, "myhome.home.soc.uninvite", true);
    }

    public static boolean canPublic(Player player) {
        return permission(player, "myhome.home.soc.public", true);
    }

    public static boolean canPrivate(Player player) {
        return permission(player, "myhome.home.soc.private", true);
    }
}
