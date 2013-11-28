package ru.gtncraft.worldprotect;

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final public class Lang {

    final public static String WORLDEDIT_NOT_INSTALLED = "Плагин WorldEdit не найден.";
    final public static String WORLDEDIT_NO_SELECTION = "Сначала выделите регион.";
    final public static String REGION_MISSING_NAME = "Введите имя региона.";
    final public static String PLAYER_NAME_MISSING = "Укажите игрока.";
    final public static String REGION_OVERLAY_WITH_ANOTHER = "Регион пересекается с другим.";
    final public static String REGION_NOT_FOUND_IN_AREA = "Регион в этой области не существует.";
    final public static String REGION_NAME = "Регион:";
    final public static String REGION_OWN_LIST = "Список регионов принадлежащих вам:";
    final public static String PLAYER_ALREADY_IN_REGION = "Игрок уже добавлен в этот регион.";
    final public static String PLAYER_NOT_FOUND_IN_REGION = "Игрока с таким именем нет в регионе.";

    final public static String FLAG_UNKNOWN = "Неизвестный флаг %s.";
    final public static String FLAG_INVALID_VALUE = "Некорректное значение флага %s.";
    final public static String FLAG_MISSING = "Введите название флага.";
    final public static String FLAG_NO_VALUE = "Введите значение флага.";
    final public static String FLAG_CHANGED = "Флаг %s для региона %s установлен.";

    final public static String REGION_NOT_FOUND = "Регион %s не найден.";
    final public static String REGION_NO_PERMISSION = "У вас нет прав на редактирование этого региона.";
    final public static String REGION_EXISTS = "Регион с именем %s уже создан.";
    final public static String REGION_CREATED = "Регион %s создан.";
    final public static String REGION_DELETED = "Регион %s удален.";
    final public static String REGION_PLAYER_ADDED = "Игрок %s добавлен в регион %s.";
    final public static String REGION_PLAYER_DELETED = "Игрок %s удален из региона %s.";
    final public static String REGION_MAX_LIMIT = "Вы создали максимальное количество регионов %d.";
    final public static String SAVE_SUCCESS = "Регионы сохранены.";

    final public static void showRegionInfo(Player sender, Region region) {
        sender.sendMessage(ChatColor.YELLOW + "Регион: "    + ChatColor.WHITE + region.getName());
        sender.sendMessage(ChatColor.YELLOW + "Размер: "    + ChatColor.WHITE + region);
        sender.sendMessage(ChatColor.YELLOW + "Владельцы: " + ChatColor.WHITE + region.get(Players.role.owner));
        sender.sendMessage(ChatColor.YELLOW + "Игроки: "    + ChatColor.WHITE + region.get(Players.role.member));
        List<String> flags = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : region.getFlags().entrySet()) {
            String value = entry.getValue() ? ChatColor.RED + "запрещено" : ChatColor.GRAY + "разрешено";
            flags.add(ChatColor.WHITE + entry.getKey() + ": " + value + ChatColor.WHITE);
        }
        sender.sendMessage(ChatColor.YELLOW + "Флаги: " + Joiner.on(", ").join(flags));
    }
}
