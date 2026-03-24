package de.gamblegamez.rucksack;

import de.gamblegamez.rucksack.command.RucksackCommand;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class Bootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
            context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                var registrar = event.registrar();
                registrar.register(RucksackCommand.command, List.of("backpack"));
            });
    }
}
