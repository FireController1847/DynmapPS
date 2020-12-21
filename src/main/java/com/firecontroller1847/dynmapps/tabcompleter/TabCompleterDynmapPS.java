package com.firecontroller1847.dynmapps.tabcompleter;

import com.firecontroller1847.dynmapps.DynmapPS;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleterDynmapPS implements TabCompleter {

    // Define Primary Arguments
    public static final String[][] FIRST_ARGS = {
        { "reload", "dynmapps.command.reload" },
        { "forceupdate", "dynmapps.command.forceupdate" }
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Completions
        ArrayList<String> completions = new ArrayList();

        // First-Level Arguments
        if (args.length == 1) {
            ArrayList<String> originals = new ArrayList<>();
            for (String[] argument : FIRST_ARGS) {
                if (DynmapPS.hasPermission(sender, argument[1])) {
                    originals.add(argument[0]);
                }
            }
            StringUtil.copyPartialMatches(args[0], originals, completions);
        }

        // Sort Completions & Return
        Collections.sort(completions);
        return completions;
    }

}
