package org.twightlight.hlootchest.listeners;


import org.bukkit.entity.Pig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageEvent implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Pig) {
            Pig pig = (Pig) event.getEntity();
            if ("LootchestPig".equals(pig.getCustomName())) {
                event.setCancelled(true);
            }
        }
    }
}
