package ru.gtncraft.worldprotect;

import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.*;

public class Translations {
    private static ResourceBundle bundle;

    static {
        try {
            bundle = ResourceBundle.getBundle("messages");
        } catch (MissingResourceException ignore) {
            bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        }
    }

    public static String get(Message message, Object... args) {
        String name = message.toString();
        String translation = "&4translation '" + name + "' missing";
        try {
            translation = MessageFormat.format(bundle.getString(name), args);
        } catch (MissingResourceException ignore) {
        }
        return ChatColor.translateAlternateColorCodes('&', translation);
    }
}
