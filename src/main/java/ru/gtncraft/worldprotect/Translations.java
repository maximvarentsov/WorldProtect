package ru.gtncraft.worldprotect;

import org.bukkit.ChatColor;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;

public class Translations {
    private static ResourceBundle bundle;

    private static ResourceBundle getBundle(String baseName) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName);
        if (!(bundle instanceof PropertyResourceBundle)) {
            return bundle;
        }
        return new UTF8PropertyResourceBundle((PropertyResourceBundle) bundle);
    }

    private static ResourceBundle getBundle(String baseName, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        if (!(bundle instanceof PropertyResourceBundle)) {
            return bundle;
        }
        return new UTF8PropertyResourceBundle((PropertyResourceBundle) bundle);
    }

    private static class UTF8PropertyResourceBundle extends ResourceBundle {
        PropertyResourceBundle propertyResourceBundle;

        private UTF8PropertyResourceBundle(PropertyResourceBundle bundle) {
            this.propertyResourceBundle = bundle;
        }

        public Enumeration getKeys() {
            return propertyResourceBundle.getKeys();
        }

        protected Object handleGetObject(String key) {
            String value = (String) propertyResourceBundle.handleGetObject(key);
            if (value != null) {
                try {
                    return new String(value.getBytes("ISO-8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException exception) {
                    throw new RuntimeException(
                            "UTF-8 encoding is not supported.", exception);
                }
            }
            return null;
        }
    }

    static {
        try {
            bundle = getBundle("messages");
        } catch (MissingResourceException ignore) {
            bundle = getBundle("messages", Locale.ENGLISH);
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
