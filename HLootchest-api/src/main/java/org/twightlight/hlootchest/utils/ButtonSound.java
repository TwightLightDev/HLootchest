package org.twightlight.hlootchest.utils;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;

public class ButtonSound {
    Sound sound;

    float yaw;

    float pitch;

    public ButtonSound(Sound sound, float yaw, float pitch) {
        this.sound = sound;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return this.sound;
    }

    public String getSoundString() {
        return XSound.matchXSound(this.sound).name();
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
