package ru.gtncraft.worldprotect;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;

import java.util.List;

public class Commands implements CommandExecutor {

    private final WorldProtect plugin;
    private final WorldEditPlugin we;
    private final int regionPerPlayer;

    private class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }

    public Commands(final WorldProtect plugin) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        this.regionPerPlayer = plugin.getConfig().getConfigurationSection("region").getInt("maxPerPlayer", 8);
        this.plugin.getCommand("region").setExecutor(this);
        this.plugin.getCommand("region").setTabCompleter(new CommandsCompleter(plugin));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if ( ! (commandSender instanceof Player) ) {
            return false;
        }
        Player player = (Player) commandSender;
        if ( ! player.hasPermission(plugin.PERMISSION_USE) ) {
            return false;
        }
        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    return commandDefine(player, args);
                case "del":
                case "delete":
                case "remove":
                    return commandDelete(player, args);
                case "addowner":
                    return addPlayer(player, args, Players.role.owner);
                case "delowner":
                case "deleteowner":
                case "removeowner":
                    return removePlayer(player, args, Players.role.owner);
                case "addmember":
                    return addPlayer(player, args, Players.role.member);
                case "delmember":
                case "deletemember":
                case "removemember":
                    return removePlayer(player, args, Players.role.member);
                case "info":
                    return commandInfo(player, args);
                case "list":
                    return commandList(player);
                case "save":
                    return commandSave(player);
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            return commandFlagSet(player, args);
                    }
                // usage for unknow subcommands.
                default:
                    return false;
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + ex.getMessage());
            return true;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    private boolean commandDefine(Player sender, String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(Lang.WORLDEDIT_NOT_INSTALLED);
        }

        Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(Lang.WORLDEDIT_NO_SELECTION);
        }

        Location p1 = selection.getMinimumPoint();
        Location p2 = selection.getMaximumPoint();

        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);

        if (plugin.getRegionManager().get(sender.getWorld(), name) != null) {
            throw new CommandException(String.format(Lang.REGION_EXISTS, name));
        }

        Region region = new Region(p1, p2);
        region.setName(name);
        region.add(sender.getName(), Players.role.owner);

        if (!sender.hasPermission(plugin.PERMISSION_ADMIN)) {
            /**
             *  Check region have overlay with another.
             */
            for (Region overlay : plugin.getRegionManager().get(p1, p2)) {
                plugin.getLogger().info(overlay + " " + overlay.getName());
                if (!overlay.has(sender, Players.role.owner)) {
                    throw new CommandException(Lang.REGION_OVERLAY_WITH_ANOTHER);
                }
            }
            /**
             * Check region per player limit.
             */
            int total = plugin.getRegionManager().get(sender, Players.role.owner).size();
            if (regionPerPlayer > 0 && total >= regionPerPlayer) {
                throw new CommandException(String.format(Lang.REGION_MAX_LIMIT, regionPerPlayer));
            }
        }

        plugin.getRegionManager().add(sender.getWorld(), region);
        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_CREATED, name));
        return true;
    }

    private boolean commandList(Player sender) {
        sender.sendMessage(ChatColor.GREEN + Lang.REGION_OWN_LIST);
        for (Region region : plugin.getRegionManager().get(sender, Players.role.owner)) {
            sender.sendMessage(ChatColor.GREEN + Lang.REGION_NAME + " " + region.getName() + " " + region.getSize());
        }
        return true;
    }

    private boolean commandInfo(Player sender, String[] args) throws CommandException {
        if (args.length == 2) {
            String name = args[1];
            Region region = plugin.getRegionManager().get(sender.getWorld(), name);
            if (region == null) {
                throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
            }
            Lang.showRegionInfo(sender, region);
            return true;
        } else if (args.length == 1) {
            List<Region> regions = plugin.getRegionManager().get(sender.getLocation());
            if (regions.size() == 0) {
                throw new CommandException(Lang.REGION_NOT_FOUND_IN_AREA);
            }
            for (Region region : regions) {
                Lang.showRegionInfo(sender, region);
            }
            return true;
        }
        return false;
    }

    private boolean commandSave(Player sender) throws CommandException {
        if (!sender.hasPermission(plugin.PERMISSION_ADMIN)) {
            return false;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    plugin.getLogger().info("Save regions for world " + world.getName() + ".");
                    plugin.getRegionManager().save(world);
                }
            }
        });
        sender.sendMessage(ChatColor.GREEN + Lang.SAVE_SUCCESS);
        return true;
    }

    private boolean commandDelete(Player sender, String[] args) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        checkPermission(sender, region);

        plugin.getRegionManager().delete(sender.getWorld(), name);
        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_DELETED, name));
        return true;
    }

    private boolean commandFlagSet(Player sender, String[] args) throws CommandException {

        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String flag = getParam(args, 3, Lang.FLAG_MISSING);

        String value = getParam(args, 4, Lang.FLAG_NO_VALUE);
        boolean valueFlag;
        // reverse values, prevent true
        switch (value.toLowerCase()) {
            case "1":
            case "on":
            case "true":
                valueFlag = false;
                break;
            case "0":
            case "off":
            case "false":
                valueFlag = true;
                break;
            default:
                throw new CommandException(String.format(Lang.FLAG_INVALID_VALUE, value));
        }

        checkPermission(sender, region);

        try {
            region.set(Flags.prevent.valueOf(flag), valueFlag);
            sender.sendMessage(ChatColor.GREEN + String.format(Lang.FLAG_CHANGED, flag, name));
        } catch (IllegalArgumentException ex) {
            throw new CommandException(String.format(Lang.FLAG_UNKNOWN, flag));
        }

        return true;
    }

    private boolean addPlayer(Player sender, String[] args, Players.role role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String player = getParam(args, 2, Lang.PLAYER_NAME_MISSING);

        checkPermission(sender, region);

        if (!region.add(player, role)) {
            throw new CommandException(Lang.PLAYER_ALREADY_IN_REGION);
        }

        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_PLAYER_ADDED, player, name));
        return true;
    }

    private boolean removePlayer(Player sender, String[] args, Players.role role) throws CommandException {
        String name = getParam(args, 1, Lang.REGION_MISSING_NAME);
        Region region = plugin.getRegionManager().get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(String.format(Lang.REGION_NOT_FOUND, name));
        }

        String player = getParam(args, 2, Lang.PLAYER_NAME_MISSING);

        checkPermission(sender, region);

        if (!region.remove(player, role)) {
            throw new CommandException(Lang.PLAYER_NOT_FOUND_IN_REGION);
        }

        sender.sendMessage(ChatColor.GREEN + String.format(Lang.REGION_PLAYER_DELETED, player, name));
        return true;
    }

    private String getParam(String args[], int index, String message) throws CommandException {
        try {
            return args[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new CommandException(message);
        }
    }

    private void checkPermission(Player sender, Region region) throws CommandException {
        if (!(region.has(sender, Players.role.owner) || sender.hasPermission(plugin.PERMISSION_ADMIN))) {
            throw new CommandException(Lang.REGION_NO_PERMISSION);
        }
    }
}
