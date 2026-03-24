package de.gamblegamez.rucksack.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.gamblegamez.rucksack.backpack.BackpackData;
import de.gamblegamez.rucksack.item.BackpackItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings({"UnstableApiUsage"})
public class RucksackCommand {
    private static void giveItem(Player player, int pages, int rows) {
        var backpackData = new BackpackData(UUID.randomUUID(), pages, rows);
        var itemStack = BackpackItem.createItem(backpackData);
        player.getInventory().addItem(itemStack);
    }

    public final static LiteralCommandNode<CommandSourceStack> command = Commands.literal("rucksack")
            .then(
                    Commands.argument("pages", IntegerArgumentType.integer(1)).then(
                            Commands.argument("rows", IntegerArgumentType.integer(1, 5)).executes(ctx -> {
                                var sender = ctx.getSource().getSender();
                                if(!(sender instanceof Player player)) {
                                    return 0;
                                }

                                var pages = IntegerArgumentType.getInteger(ctx, "pages");
                                var rows = IntegerArgumentType.getInteger(ctx, "rows");

                                giveItem(player, pages, rows);

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            )
            .requires(ctx -> ctx.getSender() instanceof Player)
            .executes(ctx -> {
                var sender = ctx.getSource().getSender();
                if(!(sender instanceof Player player)) {
                    return 0;
                }

                giveItem(player, 1, 1);

                return Command.SINGLE_SUCCESS;
            })
            .build();
}
