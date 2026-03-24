package de.gamblegamez.rucksack.item;

import de.gamblegamez.rucksack.Rucksack;
import de.gamblegamez.rucksack.backpack.BackpackData;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.*;

public class BackpackItem {
    private static final NamespacedKey ID_KEY = new NamespacedKey("backpack", "id");
    private static final NamespacedKey PAGES_KEY = new NamespacedKey("backpack", "pages");
    private static final NamespacedKey ROWS_KEY =  new NamespacedKey("backpack", "rows");

    /**
     * Memory-representation of all currently loaded backpacks (storing the serialized items).
     */
    private static final Map<UUID, Int2ObjectOpenHashMap<byte[]>> BACKPACK_DATA_MAP = new HashMap<>();

    public static boolean isBackpack(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().has(ID_KEY);
    }

    public static @Nullable BackpackData getData(ItemStack itemStack) {
        var pdc = itemStack.getPersistentDataContainer();
        var id = pdc.get(ID_KEY, new UUIDDataType());
        var pages = pdc.get(PAGES_KEY, PersistentDataType.INTEGER);
        var rows = pdc.get(ROWS_KEY, PersistentDataType.INTEGER);
        if(pages == null || rows == null) {
            return null;
        }
        return new BackpackData(id, pages, rows);
    }

    /**
     * Asynchronously fetch all pages of a backpack item, causing them to be loaded into memory.
     * <br>
     * Used to prevent blocking reads on the first time a backpack is accessed.
     */
    public static void attemptToLoadItem(ItemStack itemStack) {
        var data = getData(itemStack);
        if(data == null) {
            return;
        }
        Rucksack.runAsyncTask(() -> {
            for(int page = 0; page < data.pages(); page++) {
                getRawItems(data.id(), page);
            }
        });
    }

    public static byte[] getRawItems(UUID id, int page) {
        var pages = BACKPACK_DATA_MAP.get(id);
        if (pages != null && pages.containsKey(page)) {
            return pages.get(page);
        }

        var databaseManager = Rucksack.getDatabaseManager();
        if (databaseManager == null) {
            return null;
        }

        var data = databaseManager.getBackpackPage(id, page);
        if (data == null) {
            return null;
        }

        BACKPACK_DATA_MAP.computeIfAbsent(id, key -> new Int2ObjectOpenHashMap<>()).put(page, data);
        return data;
    }

    public static void saveItems(UUID id, List<ItemStack> items, int page) {
        var pages = BACKPACK_DATA_MAP.computeIfAbsent(id, k -> new Int2ObjectOpenHashMap<>());
        var serialized = ItemSerializer.serializeItems(items);
        pages.put(page, serialized);

        Rucksack.runAsyncTask(() -> {
            var databaseManager = Rucksack.getDatabaseManager();
            if (databaseManager != null) {
                databaseManager.saveBackpackPage(id, page, serialized);
            }
        });
    }

    public static ItemStack createItem(BackpackData data) {
        var base = ItemStack.of(Material.CHEST);

        base.editMeta(meta -> {
            // init pdc
            var pdc = meta.getPersistentDataContainer();
            pdc.set(ID_KEY, new UUIDDataType(), data.id());
            pdc.set(PAGES_KEY, PersistentDataType.INTEGER, data.pages());
            pdc.set(ROWS_KEY, PersistentDataType.INTEGER, data.rows());

            meta.itemName(Rucksack.mm("<green>Rucksack"));
        });

        return base;
    }
}
