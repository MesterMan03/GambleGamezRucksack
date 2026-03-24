package de.gamblegamez.rucksack;

import de.gamblegamez.rucksack.listener.ItemListener;
import de.gamblegamez.rucksack.persistence.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class Rucksack extends JavaPlugin {
    private static Rucksack instance;
    private boolean shuttingDown;
    private DatabaseManager databaseManager;

    public static Rucksack getInstance() {
        return instance;
    }

    public static @Nullable DatabaseManager getDatabaseManager() {
        if (instance == null) {
            return null;
        }
        return instance.databaseManager;
    }

    public static void runAsyncTask(Runnable task) {
        var plugin = getInstance();
        if (plugin == null || plugin.shuttingDown || !plugin.isEnabled()) {
            return;
        }
        Bukkit.getAsyncScheduler().runNow(plugin, (t) -> task.run() );
    }

    public Rucksack() {
        instance = this;
    }

    public static Component mm(String input) {
        return MiniMessage.miniMessage().deserialize(input);
    }

    @Override
    public void onEnable() {
        shuttingDown = false;
        saveDefaultConfig();

        try {
            databaseManager = new DatabaseManager(this);
        } catch (Exception exception) {
            getComponentLogger().error("Failed to initialize database manager", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new ItemListener(), this);
    }

    @Override
    public void onDisable() {
        shuttingDown = true;
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
