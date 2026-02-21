package org.twightlight.hlootchest.commands.main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.twightlight.hlootchest.HLootChest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainTabCompleter implements org.bukkit.command.TabCompleter {

    private final List<String> SUBCOMMANDS = Arrays.asList("help", "forceopen", "leave", "list", "open");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("forceopen"))) {
            for (String box : HLootChest.getNms().getRegistrationData().keySet()) {
                if (box.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(box);
                }
            }
        }
        return completions;
    }
}
