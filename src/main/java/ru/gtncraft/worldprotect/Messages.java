package ru.gtncraft.worldprotect;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Messages {
    private static Map<Message, String> messages = new HashMap<>();

    public static void load(ConfigurationSection config) {
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            Message key;
            try {
                key = Message.valueOf(entry.getKey());
            } catch (IllegalArgumentException ignore) {
                continue;
            }
            String value = ChatColor.translateAlternateColorCodes('&', String.valueOf(entry.getValue()));
            messages.put(key, value);
        }
    }

    public static String get(Message message, Object... args) {
        String text = messages.get(message);
        if (text == null) {
            text = message.toString();
        }
        return String.format(text, args);
    }
}
