package setadokalo.alchemicalbrewing.blocks.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;

@Environment(EnvType.CLIENT)
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleEntity> {
	private final BlockEntityRendererProvider.Context context;

	public CrucibleRenderer(BlockEntityRendererProvider.Context dispatcher) {
		this.context = dispatcher;
	}

	private static final double TAU = 6.28318530718;
	@Override
	public void render(CrucibleEntity blockEntity, float tickDelta, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light, int overlay) {
		List<ItemStack> itemsInPot = blockEntity.getItemsInPot();
		double time = Objects.requireNonNull(blockEntity.getLevel()).getGameTime() + tickDelta;
		if (itemsInPot.size() == 1) {
			ItemStack stack = itemsInPot.get(0);
			matrices.pushPose();
			double offset = Math.sin(time / 8.0) / 4.0;
			matrices.translate(0.5, 0.5 + offset, 0.5);
	
			matrices.mulPose(Vector3f.YP.rotationDegrees((blockEntity.getLevel().getGameTime() + tickDelta) * 4));
			matrices.scale(0.75f, 0.75f, 0.75f);
	
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, light, overlay, matrices, vertexConsumers, 0);
	 
			matrices.popPose();
		} else {
			double scale = (1.0 / (double) itemsInPot.size()) * TAU;
			for (int i = 0; i < itemsInPot.size(); i++) {
				ItemStack stack = itemsInPot.get(i);
				matrices.pushPose();
				double offset = Math.sin((time / 8.0 + (double) i * scale)) / 6.0;
				matrices.translate(Math.sin(i * scale + time / 24.0) * 0.3 + 0.5, 0.5 + offset, Math.cos(i * scale + time / 24.0) * 0.3 + 0.5);
		
				matrices.mulPose(Vector3f.YP.rotationDegrees((float)time * (3.0F + (float)Math.sin(i * 0.1F)) + (float)i * 103.0F));
				matrices.scale(0.75f, 0.75f, 0.75f);
		
				Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, light, overlay, matrices, vertexConsumers, 0);
		 
				matrices.popPose();
			}
		}
	}
}