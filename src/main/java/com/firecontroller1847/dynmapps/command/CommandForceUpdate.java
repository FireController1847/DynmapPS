package com.firecontroller1847.dynmapps.command;

import com.firecontroller1847.dynmapps.DynmapPS;
import com.firecontroller1847.dynmapps.FireCommand;
import com.firecontroller1847.dynmapps.FirePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandForceUpdate extends FireCommand {

    public CommandForceUpdate(FirePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        (new Thread(() -> {
            sender.sendMessage(plugin.getTranslation("forceupdate.wait"));
            ((DynmapPS) plugin).update();
            sender.sendMessage(plugin.getTranslation("forceupdate.complete"));
        })).start();

        // The command always works
        return true;
    }

}
