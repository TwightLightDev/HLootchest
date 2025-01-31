package org.twightlight.hlootchest.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.twightlight.hlootchest.HLootchest;

public class DismountEvent implements Listener {
    @EventHandler
    public void onDismountEvent(VehicleExitEvent e) {
        Entity entity = e.getVehicle();
        Entity exited = e.getExited();

        if (entity instanceof Pig && exited instanceof Player) {
            Pig pig = (Pig) entity;
            Player player = (Player) exited;
            if ("LootchestPig".equals(pig.getCustomName()) && HLootchest.getNms().getBoxFromPlayer(player) != null) {
                e.setCancelled(true);
            }
        }
    }
}
