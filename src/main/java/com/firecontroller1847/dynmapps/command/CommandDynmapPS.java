package com.firecontroller1847.dynmapps.command;

import com.firecontroller1847.dynmapps.DynmapPS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandDynmapPS implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Get plugin instance
        DynmapPS plugin = (DynmapPS) DynmapPS.getInstance();

        // Handle-Sub-Commands
        if (args.length > 0) {
            // Reload
            if (args[0].equalsIgnoreCase("reload")) {
                return DynmapPS.runCommand(new CommandReload(plugin), "dynmapps.command.reload", sender, command, label, args);
            }

            // ForceUpdate
            if (args[0].equalsIgnoreCase("forceupdate")) {
                return DynmapPS.runCommand(new CommandForceUpdate(plugin), "dynmapps.command.forceupdate", sender, command, label, args);
            }
        }

        // If it's not the command or any sub command, return false
        sender.sendMessage("/" + label + " reload");
        sender.sendMessage("/" + label + " forceupdate");
        return true;
    }

}
