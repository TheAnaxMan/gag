package ky.someone.mods.gag.client.render;

import ky.someone.mods.gag.entity.TimeAcceleratorEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity> {
	public TimeAcceleratorEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(TimeAcceleratorEntity entity) {
		return null;
	}
}
