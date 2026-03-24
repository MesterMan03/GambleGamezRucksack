package de.gamblegamez.rucksack.listener;

import de.gamblegamez.rucksack.Rucksack;
import de.gamblegamez.rucksack.item.BackpackItem;
import de.gamblegamez.rucksack.ui.BackpackUI;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {
    @EventHandler
    private void on(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var item = event.getItem();

        if(item == null || item.isEmpty()) {
            return;
        }
        if(!BackpackItem.isBackpack(item)) {
            return;
        }

        var data = BackpackItem.getData(item);
        if(data == null) {
            return;
        }
        event.setCancelled(true);

        if(!event.getAction().isRightClick()) {
            return;
        }

        Bukkit.getScheduler().runTask(Rucksack.getInstance(), () -> {
            var ui = new BackpackUI(data, 0);
            player.openInventory(ui.getInventory());
        });
    }

    @EventHandler
    private void on(PlayerInventorySlotChangeEvent event) {
        var item = event.getNewItemStack();
        if(!BackpackItem.isBackpack(item)) {
            return;
        }

        BackpackItem.attemptToLoadItem(item);
    }
}
