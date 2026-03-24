package de.gamblegamez.rucksack.ui;

import com.destroystokyo.paper.profile.ProfileProperty;
import de.gamblegamez.rucksack.Rucksack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public enum SkullType {
    ARROW_RIGHT(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY3MWM0YzA0MzM3YzM4YTVjN2YzMWE1Yzc1MWY5OTFlOTZjMDNkZjczMGNkYmVlOTkzMjA2NTVjMTlkIn19fQ==",
            "<aqua><bold>→"
    ),
    ARROW_LEFT(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTM5NzExMjRiZTg5YWM3ZGM5YzkyOWZlOWI2ZWZhN2EwN2NlMzdjZTFkYTJkZjY5MWJmODY2MzQ2NzQ3N2M3In19fQ==",
            "<aqua><bold>←"
    );

    public final String texture;
    public final String name;

    SkullType(String texture, String name) {
        this.texture = texture;
        this.name = name;
    }

    public ItemStack getItemStack() {
        var base = ItemStack.of(Material.PLAYER_HEAD);
        base.editMeta(meta -> {
            var skullMeta = (SkullMeta) meta;
            var profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", texture));
            skullMeta.setPlayerProfile(profile);

            meta.displayName(Rucksack.mm(String.format("<!i>%s", name)));
        });
        return base;
    }
}
