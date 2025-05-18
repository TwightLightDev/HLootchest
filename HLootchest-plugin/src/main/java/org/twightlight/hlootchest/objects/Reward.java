package org.twightlight.hlootchest.objects;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.utils.Utility;

import java.util.List;

public class Reward {

    private final List<String> actions;

    public Reward(List<String> actions) {
        this.actions = actions;
    }

    public void accept(Player p) {
        for (String action : actions) {
            String[] dataset = action.split(" ", 2);
            if (dataset[0].equals("[player]")) {
                p.performCommand(Utility.replaceCommand(p, dataset[1]));
            } else if (dataset[0].equals("[console]")) {
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                Bukkit.getServer().dispatchCommand(console, Utility.replaceCommand(p, dataset[1]));
            }
        }
    }
}
