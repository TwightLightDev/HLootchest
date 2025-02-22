package org.twightlight.hlootchest.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootchest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTabCompleter implements TabCompleter {
    private final List<String> SUBCOMMANDS = Arrays.asList("add", "help", "reload", "template", "templateslist", "lootchestsSetup");
    private final List<String> TemplateActions = Arrays.asList("delete", "select", "edit", "create");
    private final List<Integer> idealNums = Arrays.asList(1, 2, 3, 4, 5, 10, 20, 50, 100);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : SUBCOMMANDS) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                for (Player player : Bukkit.getOnlinePlayers().stream()
                        .limit(20)
                        .collect(Collectors.toList())) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
            if (args[0].equalsIgnoreCase("template")) {
                for (String action : TemplateActions) {
                    if (action.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(action);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                for (String box : HLootchest.getNms().getRegistrationData().keySet()) {
                    if (box.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(box);
                    }
                }
            }
            if (args[0].equalsIgnoreCase("template")) {
                File[] files = new File((HLootchest.getFilePath() + "/templates")).listFiles();

                for (File file : files) {
                    if (file.getName().replace(".yml", "").startsWith(args[2].toLowerCase())) {
                        completions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("add")) {
                for (Integer num : idealNums) {
                    if (String.valueOf(num).startsWith(args[3].toLowerCase())) {
                        completions.add(String.valueOf(num));
                    }
                }
                completions.sort(Comparator.comparingInt(Integer::parseInt));
            }
        }
        return completions;
    }
}
