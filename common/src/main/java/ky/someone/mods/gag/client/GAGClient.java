package ky.someone.mods.gag.client;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import ky.someone.mods.gag.GAGUtil;
import ky.someone.mods.gag.block.BlockRegistry;
import ky.someone.mods.gag.client.render.TimeAcceleratorEntityRenderer;
import ky.someone.mods.gag.client.screen.LabelingMenuScreen;
import ky.someone.mods.gag.config.GAGConfig;
import ky.someone.mods.gag.entity.EntityTypeRegistry;
import ky.someone.mods.gag.entity.TimeAcceleratorEntity;
import ky.someone.mods.gag.item.GAGItem;
import ky.someone.mods.gag.menu.MenuTypeRegistry;
import ky.someone.mods.gag.particle.ParticleTypeRegistry;
import ky.someone.mods.gag.particle.client.MagicParticle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface GAGClient {

	Screen DUMMY_SCREEN = new Screen(TextComponent.EMPTY) {
	};

	static void init() {
		registerEntityRenderers();

		ClientLifecycleEvent.CLIENT_SETUP.register(GAGClient::setup);
		ClientLifecycleEvent.CLIENT_STARTED.register(GAGClient::clientDone);
		ClientGuiEvent.RENDER_HUD.register(GAGClient::renderHUD);

		ParticleProviderRegistry.register(ParticleTypeRegistry.MAGIC, MagicParticle.Provider::new);
	}

	static void registerEntityRenderers() {
		EntityRendererRegistry.register(EntityTypeRegistry.TIME_ACCELERATOR, TimeAcceleratorEntityRenderer::new);
		EntityRendererRegistry.register(EntityTypeRegistry.MINING_DYNAMITE, ThrownItemRenderer::new);
	}

	static void renderHUD(PoseStack poseStack, float partialTicks) {
		var minecraft = Minecraft.getInstance();

		if (minecraft == null || minecraft.options.hideGui || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			return;
		}

		var level = minecraft.level;
		var player = minecraft.player;

		if (level == null || player == null) {
			return;
		}

		if (minecraft.hitResult instanceof BlockHitResult blockHit) {
			var pos = blockHit.getBlockPos();
			var block = level.getBlockState(pos).getBlock();

			var accelerator = Iterables.getFirst(level.getEntitiesOfClass(TimeAcceleratorEntity.class, new AABB(pos)), null);
			if (accelerator != null) {
				var accelSpeed = accelerator.getTimesAccelerated();
				var timeLeft = accelerator.getTicksRemaining() / 20d;

				if (accelSpeed == 0) return;

				renderHudTooltip(poseStack, List.of(
						block.getName(),
						new TranslatableComponent("info.gag.time_sand_tooltip_mult",
								GAGUtil.asStyledValue(accelSpeed, GAGConfig.SandsOfTime.MAX_RATE.get(), Integer.toString(1 << accelSpeed))),
						new TranslatableComponent("info.gag.time_sand_tooltip_time",
								GAGUtil.asStyledValue(timeLeft, GAGConfig.SandsOfTime.DURATION_PER_USE.get(), String.format("%.2f", timeLeft)))
				));

				return;
			}
		}

		var stack = player.getUseItem();
		List<Component> tooltip = List.of();

		if (!stack.isEmpty() && stack.getItem() instanceof GAGItem item) {
			tooltip = item.getUsingTooltip(player, stack, player.getTicksUsingItem());
		} else if ((stack = player.getMainHandItem()).getItem() instanceof GAGItem item) {
			tooltip = item.getHoldingTooltip(player, stack);
		} else if ((stack = player.getOffhandItem()).getItem() instanceof GAGItem item) {
			tooltip = item.getHoldingTooltip(player, stack);
		}

		if (!tooltip.isEmpty()) {
			renderHudTooltip(poseStack, tooltip);
		}
	}

	private static void renderHudTooltip(PoseStack poseStack, List<Component> text) {
		var mc = Minecraft.getInstance();
		var x = (DUMMY_SCREEN.width = mc.getWindow().getGuiScaledWidth()) / 2;
		var y = (DUMMY_SCREEN.height = mc.getWindow().getGuiScaledHeight()) / 2;
		DUMMY_SCREEN.renderComponentTooltip(poseStack, text, x + 10, y);
	}

	static void setup(Minecraft minecraft) {
		RenderTypeRegistry.register(RenderType.cutoutMipped(), BlockRegistry.NO_SOLICITORS_SIGN.get());
		MenuRegistry.registerScreenFactory(MenuTypeRegistry.LABELING.get(), LabelingMenuScreen::new);
	}

	static void clientDone(Minecraft minecraft) {
		DUMMY_SCREEN.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
	}
}
