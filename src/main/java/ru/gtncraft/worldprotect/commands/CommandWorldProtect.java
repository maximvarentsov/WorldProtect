package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandWorldProtect implements CommandExecutor {

    private final WorldProtect plugin;

    public CommandWorldProtect(final WorldProtect plugin) {
        this.plugin = plugin;

        final PluginCommand command = plugin.getCommand("worldprotect");
        command.setExecutor(this);
        command.setTabCompleter(new TabCompleter() {
            private final List<String> subs = ImmutableList.of(
                "save", "convert"
            );
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
                if (args.length <= 1) {
                    return partial(args[0], subs);
                }
                return ImmutableList.of();
            }
            private List<String> partial(final String token, final Collection<String> from) {
                return StringUtil.copyPartialMatches(token, from, new ArrayList<String>(from.size()));
            }
        });
        command.setPermissionMessage(plugin.getConfig().getMessage(Messages.error_no_permission));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            switch (args[0].toLowerCase()) {
                case "save":
                    return commandSave(sender);
                case "dump":
                    return commandDump(sender);
            }
        } catch (CommandException ex) {
            sender.sendMessage(ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        return false;
    }

    private boolean commandSave(final CommandSender sender) throws CommandException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    plugin.getRegionManager().save(world);
                }
            }
        });
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_saved));
        return true;
    }

    private boolean commandDump(final CommandSender sender) throws CommandException {
        final Storage storage = new JsonFile(plugin);
        for (World world : Bukkit.getServer().getWorlds()) {
            if (plugin.getConfig().useRegions(world)) {
                storage.save(world, plugin.getRegionManager().get(world));
            }
        }
        sender.sendMessage(plugin.getConfig().getMessage(Messages.success_region_dumped));
        return true;
    }
}
