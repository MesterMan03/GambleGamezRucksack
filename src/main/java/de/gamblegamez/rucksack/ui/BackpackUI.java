package de.gamblegamez.rucksack.ui;

import de.gamblegamez.rucksack.Rucksack;
import de.gamblegamez.rucksack.backpack.BackpackData;
import de.gamblegamez.rucksack.item.BackpackItem;
import de.gamblegamez.rucksack.item.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class BackpackUI implements InventoryHolder, Listener {
    private final BackpackData data;
    private final int page;
    private final boolean hasPreviousPage;
    private final boolean hasNextPage;
    private final boolean needsToolbar;

    private final Inventory inventory;

    public BackpackUI(@NonNull BackpackData data, int page) {
        this.data = data;
        this.page = page;
        this.hasPreviousPage = page > 0;
        this.hasNextPage = page + 1 < data.pages();
        this.needsToolbar = hasNextPage || hasPreviousPage;

        inventory = Bukkit.createInventory(
                this,
                (needsToolbar ? getContentRows() + 1 : getContentRows()) * 9,
                Rucksack.mm(String.format("Rucksack (%s/%s)", page + 1, data.pages()))
        );
        if(needsToolbar) {
            renderToolbar();
        }
        renderItems();

        Bukkit.getPluginManager().registerEvents(this, Rucksack.getInstance());
    }

    private int getContentRows() {
        if(!hasNextPage) {
            // data.rows is only for the last page, otherwise we assume a full page
            return data.rows();
        }
        return 5;
    }

    private void renderItems() {
        var items = getItems(page).stream().toList();
        for(int row = 0; row < getContentRows(); row++) {
            for(int col = 0; col < 9; col++) {
                var slot = row * 9 + col;
                if(slot >= items.size()) {
                    return;
                }

                var item = items.get(slot);
                if(item == null || item.isEmpty()) {
                    continue;
                }

                inventory.setItem(slot, item);
            }
        }
    }

    private void renderToolbar() {
        // use last row as toolbar
        for(int col = 0; col < 9; col++) {
            var slot = getContentRows() * 9 + col;
            if(col == 0 && hasPreviousPage) {
                inventory.setItem(slot, SkullType.ARROW_LEFT.getItemStack());
                continue;
            }
            if(col == 8 && hasNextPage) {
                inventory.setItem(slot, SkullType.ARROW_RIGHT.getItemStack());
                continue;
            }

            var toolbarItem = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE);
            toolbarItem.editMeta(meta -> meta.setHideTooltip(true));
            inventory.setItem(slot, toolbarItem);
        }
    }

    private Collection<ItemStack> getItems(int page) {
        var rawItems = BackpackItem.getRawItems(data.id(), page);
        return ItemSerializer.deserializeItems(rawItems);
    }

    private void teardown() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void on(InventoryCloseEvent event) {
        if(event.getInventory() != this.inventory) {
            return;
        }
        teardown();

        var items = new ArrayList<ItemStack>();
        for(int slot = 0; slot < (getContentRows() - 1) * 9 + 9; slot++) {
            items.add(inventory.getItem(slot));
        }
        BackpackItem.saveItems(data.id(), items, page);
    }

    @EventHandler
    private void on(InventoryClickEvent event) {
        if(!needsToolbar) {
            return;
        }

        if(event.getView().getTopInventory() != inventory) {
            return;
        }

        // prevent clicking on backpacks if a backpack ui is open
        var currentItem = event.getCurrentItem();
        if(currentItem != null && BackpackItem.isBackpack(currentItem)) {
            event.setCancelled(true);
            return;
        }

        if(event.getClickedInventory() != this.inventory) {
            return;
        }

        // check if we're clicking on toolbar
        var slot = event.getSlot();
        if(slot / 9 < getContentRows()) {
            return;
        }

        event.setCancelled(true);

        var player = event.getWhoClicked();

        var col = slot % 9;
        if(col == 0 && hasPreviousPage) {
            var ui = new BackpackUI(data, page - 1);
            Bukkit.getScheduler().runTask(Rucksack.getInstance(), () -> player.openInventory(ui.getInventory()));
        }
        if(col == 8 && hasNextPage) {
            var ui = new BackpackUI(data, page + 1);
            Bukkit.getScheduler().runTask(Rucksack.getInstance(), () -> player.openInventory(ui.getInventory()));
        }
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }
}
