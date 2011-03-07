package me.taylorkelly.myhome;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeLogger {

    public static final Logger log = Logger.getLogger("Minecraft");

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[MYHOME] " + string, ex);

    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[MYHOME] " + string);
    }

    static void info(String string) {
        log.log(Level.INFO, "[MYHOME] " + string);
    }

    static void warning(String string) {
        log.log(Level.WARNING, "[MYHOME] " + string);
    }
}
