package ru.gtncraft.worldprotect;

import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.commands.Commands;
import ru.gtncraft.worldprotect.listeners.Listeners;

import java.io.IOException;

public final class WorldProtect extends JavaPlugin {

    ProtectionManager manager;
    Config config;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        config = new Config(super.getConfig());

        try {
            manager = new ProtectionManager(this);
        } catch (IOException ex) {
            getLogger().severe(ex.getMessage());
            setEnabled(false);
            return;
        }

        new Listeners(this);
        new Commands(this);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onDisable() {
        getProtectionManager().disable();
        getServer().getScheduler().cancelTasks(this);
    }
    /**
     * API
     */
    public ProtectionManager getProtectionManager() {
        return manager;
    }
}
