package org.twightlight.hlootchest.sessions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ChatSessions {
    private static final Map<Player, ChatSessions> activeSessions = new HashMap<>();
    private final Player player;
    private Consumer<String> inputHandler;

    public ChatSessions(Player player) {
        this.player = player;
        activeSessions.put(player, this);
    }

    public void prompt(String message, Consumer<String> handler) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        this.inputHandler = handler;
    }

    public void prompt(List<String> message, Consumer<String> handler) {
        message.forEach( (text) -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', text)));
        this.inputHandler = handler;
    }

    public void handleInput(String input) {
        if (inputHandler != null) {inputHandler.accept(input);
        }
    }

    public void end() {
        activeSessions.remove(player);
    }

    public static ChatSessions getSession(Player player) {
        return activeSessions.get(player);
    }
}
