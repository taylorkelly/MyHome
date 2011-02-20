package me.taylorkelly.myhome;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.myhome.griefcraft.Updater;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class MyHome extends JavaPlugin {
    private MHEntityListener entityListener;
    private MHPlayerListener playerListener;
    private HomeList homeList;
    private boolean warning = false;

    public final String name = this.getDescription().getName();
    public final String version = this.getDescription().getVersion();

    private Updater updater;

    public MyHome(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        updater = new Updater();

    }

    public void onDisable() {
        ConnectionManager.freeConnection();
    }

    public void onEnable() {
        Logger log = Logger.getLogger("Minecraft");

        try {
            updater.check();
            updater.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Connection conn = ConnectionManager.initializeConnection(getServer());
        if (conn == null) {
            log = Logger.getLogger("Minecraft");
            log.log(Level.SEVERE, "[MYHOME] Could not establish SQL connection. Disabling MyHome");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (new File("MyWarp").exists() && new File("MyWarp", "warps.db").exists()) {
            updateFiles();
        }

        homeList = new HomeList(getServer());
        playerListener = new MHPlayerListener(homeList);
        entityListener = new MHEntityListener(homeList);

        HomePermissions.initialize(getServer());
        HomeSettings.initialize(getDataFolder());

        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);

        log.info(name + " " + version + " enabled");
    }

    private void updateFiles() {
        File file = new File("MyWarp", "warps.db");
        File folder = new File("MyWarp");
        file.renameTo(new File("homes-warps.db"));
        folder.delete();
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] split = args;
        String commandName = command.getName().toLowerCase();

        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (commandName.equals("home")) {
                /**
                 * /home
                 */
                if (split.length == 0 && HomePermissions.home(player)) {
                    if (homeList.playerHasHome(player)) {
                        homeList.sendPlayerHome(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "You have no home :(");
                        player.sendMessage("Use: " + ChatColor.RED + "/home set" + ChatColor.WHITE + " to set a home");
                    }
                    /**
                     * /home convert
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("convert") && HomePermissions.isAdmin(player)) {
                    if (!warning) {
                        player.sendMessage(ChatColor.RED + "Warning: " + ChatColor.WHITE + "Only use a copy of homes.txt.");
                        player.sendMessage("This will delete the homes.txt it uses");
                        player.sendMessage("Use " + ChatColor.RED + "'/home convert'" + ChatColor.WHITE + " again to confirm.");
                        warning = true;
                    } else {
                        Converter.convert(player, getServer(), homeList);
                        warning = false;
                    }
                    /**
                     * /home set
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("set") && HomePermissions.set(player)) {
                    homeList.addHome(player);
                    /**
                     * /home delete
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("delete") && HomePermissions.delete(player)) {
                    homeList.deleteHome(player);
                    /**
                     * /home list
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("list") && HomePermissions.list(player)) {
                    homeList.list(player);
                    /**
                     * /home ilist
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("ilist") && HomePermissions.list(player)) {
                    homeList.ilist(player);
                    /**
                     * /home private
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("private") && HomePermissions.canPrivate(player)) {
                    homeList.privatize(player);
                    /**
                     * /home public
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("public") && HomePermissions.canPublic(player)) {
                    homeList.publicize(player);
                    /**
                     * /home invite <player>
                     */
                } else if (split.length == 2 && split[0].equalsIgnoreCase("invite") && HomePermissions.invite(player)) {
                    Player invitee = getServer().getPlayer(split[1]);
                    String inviteeName = (invitee == null) ? split[1] : invitee.getName();

                    homeList.invite(player, inviteeName);
                    /**
                     * /home uninvite <player>
                     */
                } else if (split.length == 2 && split[0].equalsIgnoreCase("uninvite") && HomePermissions.uninvite(player)) {
                    Player invitee = getServer().getPlayer(split[1]);
                    String inviteeName = (invitee == null) ? split[1] : invitee.getName();

                    homeList.uninvite(player, inviteeName);
                    /**
                     * /home <name>
                     */
                } else if (split.length == 1 && split[0].equalsIgnoreCase("help")) {
                    ArrayList<String> messages = new ArrayList<String>();
                    messages.add(ChatColor.RED + "-------------------- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " --------------------");
                    if (HomePermissions.home(player)) {
                        messages.add(ChatColor.RED + "/home" + ChatColor.WHITE + "  -  Go home young chap!");
                    }
                    if (HomePermissions.set(player)) {
                        messages.add(ChatColor.RED + "/home set" + ChatColor.WHITE + "  -  Sets your home to your current position");
                    }
                    if (HomePermissions.delete(player)) {
                        messages.add(ChatColor.RED + "/home delete" + ChatColor.WHITE + "  -  Deletes your current home");
                    }
                    if (HomePermissions.homeOthers(player)) {
                        messages.add(ChatColor.RED + "/home <player>" + ChatColor.WHITE + "  -  Go to " + ChatColor.GRAY + "<player>" + ChatColor.WHITE
                                + "'s house (if allowed)");
                    }
                    if (HomePermissions.list(player)) {
                        messages.add(ChatColor.RED + "/home list" + ChatColor.WHITE + "  -  List the homes that you are invited to");
                        messages.add(ChatColor.RED + "/home ilist" + ChatColor.WHITE + "  -  List the people invited to your home");
                    }
                    if (HomePermissions.invite(player)) {
                        messages.add(ChatColor.RED + "/home invite <player>" + ChatColor.WHITE + "  -  Invite " + ChatColor.GRAY + "<player>" + ChatColor.WHITE
                                + " to your house");
                    }
                    if (HomePermissions.uninvite(player)) {
                        messages.add(ChatColor.RED + "/home uninvite <player>" + ChatColor.WHITE + "  -  Uninvite " + ChatColor.GRAY + "<player>"
                                + ChatColor.WHITE + " to your house");
                    }
                    if (HomePermissions.canPublic(player)) {
                        messages.add(ChatColor.RED + "/home public" + ChatColor.WHITE + "  -  Makes your house public");
                    }
                    if (HomePermissions.canPrivate(player)) {
                        messages.add(ChatColor.RED + "/home private" + ChatColor.WHITE + "  -  Makes your house private");
                    }
                    for (String message : messages) {
                        player.sendMessage(message);
                    }
                } else if (split.length == 1 && HomePermissions.homeOthers(player)) {
                    // TODO ChunkLoading
                    String name = split[0];
                    homeList.warpTo(name, player);
                } else {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static void setCompass(Player player, Location location) {
        if (HomeSettings.compassPointer) {
            player.setCompassTarget(location);
        }
    }

    public static Connection getConnection() {
        return ConnectionManager.getConnection();
    }
}
