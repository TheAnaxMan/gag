package ky.someone.mods.gag.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.item.TemporalPouchItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import static ky.someone.mods.gag.item.TemporalPouchItem.getStoredGrains;
import static ky.someone.mods.gag.item.TemporalPouchItem.setStoredGrains;

public class GAGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("gag")
                .then(Commands.literal("give_time")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                        .requires(source -> source.hasPermission(2))
                                        .executes(ctx -> {
                                            var player = EntityArgument.getPlayer(ctx, "player");
                                            var time = IntegerArgumentType.getInteger(ctx, "ticks");

                                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                                ItemStack stack = player.getInventory().getItem(i);
                                                if (stack.getItem() instanceof TemporalPouchItem) {
                                                    var storedGrains = getStoredGrains(stack);
                                                    var newTime = Math.min(storedGrains + time, GAGConfig.SandsOfTime.POUCH_CAPACITY.get());
                                                    setStoredGrains(stack, newTime);
                                                    player.inventoryMenu.broadcastChanges();

                                                    ctx.getSource().sendSuccess(new TranslatableComponent("commands.gag.give_time.success", time, player.getDisplayName()), true);
                                                    return 1;
                                                }
                                            }

                                            ctx.getSource().sendFailure(new TranslatableComponent("commands.gag.give_time.no_pouch", player.getDisplayName()));
                                            return 0;
                                        })
                                )
                        )));
    }
}
