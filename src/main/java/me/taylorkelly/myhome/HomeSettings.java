package me.taylorkelly.myhome;

import java.io.File;


public class HomeSettings {
    
    private static final String settingsFile = "MyHome.settings";
   
    public static boolean compassPointer;
    public static int coolDown;
    public static boolean coolDownNotify;
    public static int warmUp;
    public static boolean warmUpNotify;
    public static boolean respawnToHome;

    public static void initialize(File dataFolder) {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile  = new File(dataFolder, settingsFile);
        PropertiesFile file = new PropertiesFile(configFile);
        compassPointer = file.getBoolean("compassPointer", true, "Whether or not users' compasses point to home");
        coolDown = file.getInt("coolDown", 0, "The number of seconds between when users can go to a home");
        warmUp = file.getInt("warmUp", 0, "The number of seconds after a user uses a home command before it takes them");
        coolDownNotify = file.getBoolean("coolDownNotify", false, "Whether or not players will be notified after they've cooled down");
        warmUpNotify = file.getBoolean("warmUpNotify", true, "Whether or not players will be notified after they've warmed up");
        respawnToHome = file.getBoolean("respawnToHome", true, "Whether or not players will respawn to their homes (false means to global spawn)");
        file.save();
    }
}
