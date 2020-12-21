package com.firecontroller1847.dynmapps.command;

import com.firecontroller1847.dynmapps.FireCommand;
import com.firecontroller1847.dynmapps.FirePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandReload extends FireCommand {

    public CommandReload(FirePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload Plugin
        plugin.reload();

        // Send message
        sender.sendMessage(plugin.getTranslation("config.reloaded"));

        // The command always works
        return true;
    }

}
