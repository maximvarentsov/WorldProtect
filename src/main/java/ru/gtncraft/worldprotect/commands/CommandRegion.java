package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Region;
import ru.gtncraft.worldprotect.region.Role;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.gtncraft.worldprotect.util.Commands.getParam;
import static ru.gtncraft.worldprotect.util.Commands.getPlayer;
import static ru.gtncraft.worldprotect.util.Strings.partial;

class CommandRegion implements CommandExecutor, TabCompleter {

    final Config config;
    final ProtectionManager regions;
    final WorldEditPlugin we;
    final Collection<String> commands = ImmutableList.of(
        "define", "delete", "addowner", "deleteowner", "addmemeber", "deletemember", "info", "list", "flag", "help"
    );

    public CommandRegion(final WorldProtect plugin) {
        this.config = plugin.getConfig();
        this.regions = plugin.getProtectionManager();
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        PluginCommand command = plugin.getCommand("region");
        command.setExecutor(this);
        command.setPermissionMessage(config.getMessage(Messages.error_no_permission));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String label, String[] args) {
        /**
         * Remember that we can return null to default to online player name matching.
         */
        if (args.length > 1) {
            final String lastArg = args[args.length - 1];
            final String command = args[0].toLowerCase();
            switch (args.length) {
                case 2:
                    if (commands.contains(command) && !"list".equals(command)) {
                        return partial(lastArg, allRegions((Player) sender));
                    }
                    break;
                case 3:
                    final String region = args[1];
                    switch (command) {
                        case "deleteowner":
                            return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Role.owner));
                        case "deletemember":
                            return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Role.member));
                        case "addowner":
                        case "addmember":
                            return null;
                        case "flag":
                            return partial(lastArg, ImmutableList.of("set"));
                    }
                    break;
                case 4:
                    if ("flag".equals(command)) {
                        return partial(lastArg, Prevent.toArray());
                    }
                    break;
                case 5:
                    if ("flag".equals(command)) {
                        return partial(lastArg, ImmutableList.of("true", "false"));
                    }
                    break;
            }
        } else if (args.length <= 1) {
            return partial(args[0], commands);
        }
        return ImmutableList.of();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        boolean help = false;

        if (!(sender instanceof Player)) {
            return true;
        }

        final Player player = (Player) sender;

        if (!config.useRegions(player.getWorld())) {
            player.sendMessage(config.getMessage(Messages.error_regions_disabled, player.getWorld().getName()));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    define(player, args);
                    break;
                case "delete":
                    delete(player, args);
                    break;
                case "addowner":
                    addPlayer(player, args, Role.owner);
                    break;
                case "deleteowner":
                    removePlayer(player, args, Role.owner);
                    break;
                case "addmember":
                    addPlayer(player, args, Role.member);
                    break;
                case "deletemember":
                    removePlayer(player, args, Role.member);
                    break;
                case "info":
                    info(player, args);
                    break;
                case "list":
                    list(player);
                    break;
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            flag(player, args);
                            break;
                    }
                case "help":
                    help = true;
                    break;
                default:
                    player.sendMessage(config.getMessage(Messages.error_unknown_command, commandLabel));
                    break;
            }
        } catch (CommandException ex) {
            player.sendMessage(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ignore) {
            player.sendMessage(config.getMessage(Messages.error_unknown_command, commandLabel));
        }
        return help ? false : true;
    }

    private void define(final Player sender, final String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(config.getMessage(Messages.error_worldedit_not_found));
        }

        final Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(config.getMessage(Messages.error_region_selection));
        }

        final Location p1 = selection.getMinimumPoint();
        final Location p2 = selection.getMaximumPoint();

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_name)
        ));

        if (regions.get(sender.getWorld(), name).isPresent()) {
            throw new CommandException(config.getMessage(Messages.error_region_name_exists, name));
        }

        Region region = new Region(p1, p2);
        region.setName(name);
        region.playerAdd(sender.getUniqueId(), Role.owner);

        if (!sender.hasPermission(Permissions.admin)) {
            /**
             *  Check region have overlay with another.
             */
            if (regions.get(region).filter(rg -> !rg.player(sender.getUniqueId(), Role.owner)).findAny().isPresent()) {
                throw new CommandException(config.getMessage(Messages.error_region_overlay));
            }
            if (!sender.hasPermission(Permissions.moder)) {
                /**
                 * Check region per player limit.
                 */
                final long max = config.getLong("region.maxPerPlayer");
                final long total = regions.get(sender, Role.owner).count();
                if (max > 0 && total >= max) {
                    throw new CommandException(
                        config.getMessage(Messages.error_region_created_max, max)
                    );
                }
                /**
                 * Check region volume
                 */
                final int maxVolume = config.getInt("region.maxVolume");
                if (region.volume() > maxVolume) {
                    throw new CommandException(
                            config.getMessage(Messages.error_region_max_volume, maxVolume, region.volume())
                    );
                }
            }
        }
        regions.add(sender.getWorld(), region);
        sender.sendMessage(config.getMessage(Messages.success_region_created, name));
    }

    void list(final Player sender) {
        sender.sendMessage(config.getMessage(Messages.region_own_list) + ":");
        regions.get(sender, Role.owner).forEach((region) -> sender.sendMessage(
            config.getMessage(Messages.region_name) + ": " + region.getName() + " " + region.getSize()
        ));
    }

    void info(final Player sender, final String[] args) throws CommandException {
        if (args.length > 1) {
            String name = args[1];
            Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_not_found, name))
            );
            sender.sendMessage(config.getMessage(region));
        } else {
            Optional<Collection<Region>> values = regions.get(sender.getLocation());
            if (values.isPresent()) {
                for (Region region : values.get()) {
                    sender.sendMessage(config.getMessage(region));
                }
            } else {
                sender.sendMessage(config.getMessage(Messages.error_region_not_found));
            }
        }
    }

    void delete(final Player sender, final String[] args) throws CommandException {

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
            config.getMessage(Messages.error_input_region_name)
        ));

        Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
            config.getMessage(Messages.error_input_region_not_found, name)
        ));

        checkPermission(sender, region);

        regions.delete(sender.getWorld(), name);
        sender.sendMessage(config.getMessage(Messages.success_region_deleted, name));
    }

    void flag(final Player sender, final String[] args) throws CommandException {

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_name)
        ));

        Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_not_found, name)
        ));

        String flag = getParam(args, 3).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_flag)
        ));

        String value = getParam(args, 4).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_flag_value)
        ));

        boolean valueFlag;
        // reverse values, prevent true
        switch (value.toLowerCase()) {
            case "true":
                valueFlag = false;
                break;
            case "false":
                valueFlag = true;
                break;
            default:
                throw new CommandException(config.getMessage(Messages.error_input_flag_invalid_value));
        }

        Prevent prevent;
        try {
            prevent = Prevent.valueOf(flag);
        } catch (IllegalArgumentException ex) {
            throw new CommandException(config.getMessage(Messages.error_input_flag_unknown, flag));
        }

        boolean perms = false;

        if (sender.hasPermission(Permissions.admin)) {
            perms = true;
        } else if (sender.hasPermission(Permissions.moder) || region.player(sender.getUniqueId(), Role.owner)) {
            if (config.isAllowedFlag(prevent)) {
                perms = true;
            }
        }

        if (perms) {
            region.set(prevent, valueFlag);
            sender.sendMessage(config.getMessage(Messages.success_region_flag_set, flag, name));
        } else {
            throw new CommandException(config.getMessage(Messages.error_no_permission));
        }
    }

    void addPlayer(final Player sender, final String[] args, final Role role) throws CommandException {

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_name)
        ));

        Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_not_found, name)
        ));

        OfflinePlayer player = getPlayer(args, 2);

        checkPermission(sender, region);

        if (!region.playerAdd(player.getUniqueId(), role)) {
            throw new CommandException(config.getMessage(Messages.error_region_contains_player, player.getName()));
        }
        sender.sendMessage(config.getMessage(Messages.success_region_player_add, player.getName(), name));
    }

    void removePlayer(final Player sender, final String[] args, final Role role) throws CommandException {

        String name = getParam(args, 1).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_name)
        ));

        Region region = regions.get(sender.getWorld(), name).orElseThrow(() -> new CommandException(
                config.getMessage(Messages.error_input_region_not_found, name)
        ));

        OfflinePlayer player = getPlayer(args, 2);

        checkPermission(sender, region);

        if (!region.playerDelete(player.getUniqueId(), role)) {
            throw new CommandException(config.getMessage(Messages.error_region_player_exists, player.getName(), name));
        }

        sender.sendMessage(config.getMessage(Messages.success_region_player_delete, player.getName(), name));
    }

    void checkPermission(final Player sender, final Region region) throws CommandException {
        if (!(sender.hasPermission(Permissions.admin) || region.player(sender.getUniqueId(), Role.owner))) {
            throw new CommandException(config.getMessage(Messages.error_no_permission));
        }
    }

    Collection<String> allRegions(final Player player) {
        if (player.hasPermission(Permissions.admin)) {
            return regions.get(player.getWorld()).map(Region::getName).collect(Collectors.toList());
        }
        return regions.get(player, Role.owner).map(Region::getName).collect(Collectors.toList());
    }

    Collection<String> allRegionPlayers(final World world, final String name, final Role role) {
        Collection<String> result = new LinkedList<>();
        regions.get(world, name).ifPresent(
                region -> region.players(role).stream().map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).forEach(result::add)
        );
        return result;
    }
}
