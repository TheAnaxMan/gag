package ky.someone.mods.gag.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.item.PigmentJarItem;
import ky.someone.mods.gag.item.TemporalPouchItem;
import ky.someone.mods.gag.misc.Pigment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static ky.someone.mods.gag.item.TemporalPouchItem.getStoredGrains;
import static ky.someone.mods.gag.item.TemporalPouchItem.setStoredGrains;

public class GAGCommands {

	public static final SimpleCommandExceptionType INVALID_COLOR = new SimpleCommandExceptionType(Component.literal("Invalid rgb!"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection selection) {
		dispatcher.register(Commands.literal("gag")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("pigment_jar")
						.then(Commands.argument("rgb", StringArgumentType.word())
								.then(Commands.argument("amount", IntegerArgumentType.integer(1, PigmentJarItem.MAX_AMOUNT))
										.executes(ctx -> {
											var clrString = StringArgumentType.getString(ctx, "rgb");
											var amount = IntegerArgumentType.getInteger(ctx, "amount");

											try {
												var color = Integer.parseInt(clrString, 16);
												var stack = Pigment.ofRgb(color, amount).asJar();
												var player = ctx.getSource().getPlayerOrException();

												if (player.addItem(stack)) {
													player.containerMenu.broadcastChanges();
												} else {
													var asEntity = player.drop(stack, false);
													if (asEntity != null) {
														asEntity.setNoPickUpDelay();
														asEntity.setOwner(player.getUUID());
													}
												}

												return 1;
											} catch (NumberFormatException e) {
												throw INVALID_COLOR.create();
											}
										})
								)
						)
				)
				.then(Commands.literal("give_time")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("ticks", IntegerArgumentType.integer(0))
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

													ctx.getSource().sendSuccess(Component.translatable("commands.gag.give_time.success", time, player.getDisplayName()), true);
													return 1;
												}
											}

											ctx.getSource().sendFailure(Component.translatable("commands.gag.give_time.no_pouch", player.getDisplayName()));
											return 0;
										})
								)
						)));
	}
}
