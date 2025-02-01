package org.twightlight.hlootchest.listeners;


import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageEvent implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            if ("LootchestVehicle".equals(armorStand.getCustomName())) {
                event.setCancelled(true);
            }
        }
    }
}
