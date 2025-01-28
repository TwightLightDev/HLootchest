package org.twightlight.hlootchest.sessions.setup;

import org.bukkit.entity.Player;
import org.twightlight.hlootchest.api.objects.TConfigManager;
import org.twightlight.hlootchest.sessions.SessionManager;
import org.twightlight.hlootchest.api.enums.SessionType;

public class SetupSession extends SessionManager {

    private static boolean isInTypeChat = false;
    private static String input;

    public SetupSession(Player p, TConfigManager file, String name) {
        super(p, file, name);
        type = SessionType.SETUP;
        sessionData.put(p, this);
    }

    public void save() {
        super.save();
        getPlayer().sendMessage("Successfully saved your template!");
        file.save();
        file.reload();
    }

    public void newTypeChatSession(String inputpath) {
        isInTypeChat = true;
        input = inputpath;
    }

    public void closeTypeChatSession(String value) {
        isInTypeChat = false;
        if (value != null)
            file.set(input, value);
    }

    public boolean isInTypeChat() {
        return isInTypeChat;
    }

    public String getInputPath() {
        return input;
    }
}
