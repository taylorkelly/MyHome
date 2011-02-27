package me.taylorkelly.myhome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class MyHome extends JavaPlugin {

    private MHEntityListener entityListener;
    private MHPlayerListener playerListener;
    private HomeList homeList;
    private boolean warning = false;
    public String name;
    public String version;
    private Updater updater;
    public static final Logger log = Logger.getLogger("Minecraft");


    @Override
    public void onDisable() {
        ConnectionManager.closeConnection();
    }

    @Override
    public void onEnable() {
        name = this.getDescription().getName();
        version = this.getDescription().getVersion();

        updater = new Updater();
        try {
            updater.check();
            updater.update();
        } catch (Exception e) {
        }


        File newDatabase = new File(getDataFolder(), "homes.db");
        File oldDatabase = new File("homes-warps.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            updateFiles(oldDatabase, newDatabase);
        }

        Connection conn = ConnectionManager.initialize(this.getDataFolder());
        if (conn == null) {
            severe("Could not establish SQL connection. Disabling MyHome");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        homeList = new HomeList(getServer());
        playerListener = new MHPlayerListener(homeList, getServer());
        entityListener = new MHEntityListener(homeList);

        HomePermissions.initialize(getServer());
        HomeHelp.initialize(this);
        HomeSettings.initialize(getDataFolder());

        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Monitor, this);

        log.info(name + " " + version + " enabled");
    }

    private void updateFiles(File oldDatabase, File newDatabase) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (newDatabase.exists()) {
            newDatabase.delete();
        }
        try {
            newDatabase.createNewFile();
        } catch (IOException ex) {
            severe("Could not create new database file", ex);
        }
        copyFile(oldDatabase, newDatabase);
    }

    /**
     * File copier from xZise
     * @param fromFile
     * @param toFile
     */
    private static void copyFile(File fromFile, File toFile) {
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            Logger.getLogger(MyHome.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String[] split = args;
        String commandName = command.getName().toLowerCase();
        if (sender instanceof Player) {
            Player player = (Player) sender;
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
                    String playerName = split[0];
                    homeList.warpTo(playerName, player);
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

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[MYHOME]" + string, ex);

    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[MYHOME]" + string);
    }
}
