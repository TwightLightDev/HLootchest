package org.twightlight.hlootchest.setup.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.twightlight.hlootchest.HLootChest;
import org.twightlight.hlootchest.sessions.ChatSessions;
import org.twightlight.hlootchest.utils.Utility;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class ChatPrompt {

    @FunctionalInterface
    public interface ValueConsumer {
        void accept(String value);
    }

    public static void promptString(Player p, Runnable onRefresh, ValueConsumer onAccept) {
        prompt(p, Arrays.asList("&aType the value you want: ", "&aType 'cancel' to cancel!"),
                null, onRefresh, onAccept);
    }

    public static void promptString(Player p, List<String> messages, Runnable onRefresh, ValueConsumer onAccept) {
        prompt(p, messages, null, onRefresh, onAccept);
    }

    public static void promptNumeric(Player p, Runnable onRefresh, ValueConsumer onAccept) {
        prompt(p, Arrays.asList("&aType the value you want: ", "&aType 'cancel' to cancel!"),
                input -> {
                    if (!Utility.isNumeric(input)) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Value! Cancel the action!"));
                        return false;
                    }
                    return true;
                }, onRefresh, onAccept);
    }

    public static void prompt(Player p, List<String> messages, Function<String, Boolean> validator,
                              Runnable onRefresh, ValueConsumer onAccept) {
        p.closeInventory();
        ChatSessions sessions = new ChatSessions(p);
        sessions.prompt(messages, input -> {
            if ("cancel".equalsIgnoreCase(input)) {
                sessions.end();
                HLootChest.getScheduler().runTask(onRefresh);
                return;
            }
            if (validator != null && !validator.apply(input)) {
                sessions.end();
                HLootChest.getScheduler().runTask(onRefresh);
                return;
            }
            sessions.end();
            HLootChest.getScheduler().runTask(() -> onAccept.accept(input));
        });
    }
}

