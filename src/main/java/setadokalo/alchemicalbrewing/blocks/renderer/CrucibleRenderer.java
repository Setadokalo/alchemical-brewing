package setadokalo.alchemicalbrewing.blocks.renderer;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3f;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;

@Environment(EnvType.CLIENT)
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleEntity> {
	private BlockEntityRendererFactory.Context context;

	public CrucibleRenderer(BlockEntityRendererFactory.Context dispatcher) {
		this.context = dispatcher;
	}

	private static final double TAU = 6.28318530718;
	@Override
	public void render(CrucibleEntity blockEntity, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, int overlay) {
		List<ItemStack> itemsInPot = blockEntity.getItemsInPot();
		double time = blockEntity.getWorld().getTime() + tickDelta;
		if (itemsInPot.size() == 1) {
			ItemStack stack = itemsInPot.get(0);
			matrices.push();
			double offset = Math.sin(time / 8.0) / 4.0;
			matrices.translate(0.5, 0.5 + offset, 0.5);
	
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((blockEntity.getWorld().getTime() + tickDelta) * 4));
			matrices.scale(0.75f, 0.75f, 0.75f);
	
			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
	 
			matrices.pop();
		} else {
			double scale = (1.0 / (double) itemsInPot.size()) * TAU;
			for (int i = 0; i < itemsInPot.size(); i++) {
				ItemStack stack = itemsInPot.get(i);
				matrices.push();
				double offset = Math.sin((time / 8.0 + (double) i * scale)) / 6.0;
				matrices.translate(Math.sin(i * scale + time / 24.0) * 0.3 + 0.5, 0.5 + offset, Math.cos(i * scale + time / 24.0) * 0.3 + 0.5);
		
				matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)time * (3.0F + (float)Math.sin(i * 0.1F)) + (float)i * 103.0F));
				matrices.scale(0.75f, 0.75f, 0.75f);
		
				MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
		 
				matrices.pop();
			}
		}
	}
}