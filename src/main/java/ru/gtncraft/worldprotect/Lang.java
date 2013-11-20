package ru.gtncraft.worldprotect;

final public class Lang {

    final public static String WORLDEDIT_NOT_INSTALLED = "Плагин WorldEdit не найден.";
    final public static String WORLDEDIT_NO_SELECTION = "Сначала выделите регион.";

    final public static String REGION_MISSING_NAME = "Введите имя региона.";
    final public static String REGION_MISSING_OWNER = "Введите владельца региона.";
    final public static String REGION_MISSING_MEMBER = "Укажите игрока.";
    final public static String YOU_DONT_OWN_THIS_REGION = "Вы не можете удалить этот регион.";
    final public static String REGION_OVERLAY_WITH_ANOTHER = "Регион пересекается с другим.";
    final public static String REGION_ERROR_MEMBER_DELETE = "You don't have persmission to delete member in this region.";
    final public static String REGION_ERROR_MEMBER_ADD = "You don't have persmission to add member in this region.";

    final public static String REGION_IS_PROTECTED = "Вы не можете делать это в данном регионе.";
    final public static String REGION_NOT_FOUND = "Регион с таким именем не существует.";
    final public static String REGION_NOT_FOUND_2  = "Регион в этой области не существует.";
    final public static String REGION_OWNERS = "Владельцы:";
    final public static String REGION_MEMBERS = "Игроки:";
    final public static String REGION_FLAGS = "Флаги:";
    final public static String REGION_NAME = "Регион:";
    final public static String REGION_SIZE = "Размер:";
    final public static String REGION_OWN_LIST = "Список регионов принадлежащих вам:";
    final public static String PLAYER_ALREADY_IN_REGION = "Игрок уже добавлен в этот регион.";
    final public static String PLAYER_NOT_FOUND_IN_REGION = "Игрока с таким именем нет в регионе.";

    final public static String regionNotFound(String name) {
        return "Регион '" + name + "' не найден.";
    }

    final public static String regionExists(String name) {
        return "Регион с именем '" + name + "' уже был создан.";
    }

    final public static String regionSuccessCreated(String name) {
        return "Регион '" + name + "' создан.";
    }

    final public static String regionSuccessDeleted(String name) {
        return "Регион '" + name + "' удален.";
    }

    final public static String regionSuccessOwnerAdded(String player, String name) {
        return  "Владелец '" + player + "' добавлен в регион '" + name + "'.";
    }

    final public static String regionSuccessOwnerRemoved(String player, String name) {
        return "Владелец '" + player + "' удален из региона '" + name + "'.";
    }

    final public static String regionSuccessMemberAdded(String player, String name) {
        return "Игрок '" + player + "' добавлен в регион '" + name + "'.";
    }

    final public static String regionSuccessMemberRemoved(String player, String name) {
        return "Игрок '" + player + "' удален из региона '" + name + "'.";
    }
}
