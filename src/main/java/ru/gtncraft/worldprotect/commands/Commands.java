package ru.gtncraft.worldprotect.commands;

import ru.gtncraft.worldprotect.WorldProtect;

public class Commands {

    public Commands(final WorldProtect plugin) {
        new CommandRegion(plugin);
        new CommandWorldProtect(plugin);
    }

}
