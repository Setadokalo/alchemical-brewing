package setadokalo.alchemicalbrewing.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.renderer.CrucibleRenderer;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;
import setadokalo.alchemicalbrewing.item.FilledVial;
import setadokalo.alchemicalbrewing.util.Color;
import setadokalo.alchemicalbrewing.util.FluidEffectUtil;

@Environment(EnvType.CLIENT)
public class AlchemicalBrewingClient implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      BlockRenderLayerMap.INSTANCE.putBlock(AlchemicalBrewing.STONE_CRUCIBLE, RenderLayer.getTranslucent());
      BlockEntityRendererRegistry.INSTANCE.register(AlchemicalBrewing.crucibleBlockEntity, CrucibleRenderer::new);
      ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if (view != null && state.getBlock() == AlchemicalBrewing.STONE_CRUCIBLE) {
				BlockEntity be = view.getBlockEntity(pos);
				if (be == null) {
					return Color.WATER.asInt();
				}
				return FluidEffectUtil.getColorForEffects(((CrucibleEntity) be).getLevel() * 9000, ((CrucibleEntity)be).getEffects(), null);
			}
			return Color.WATER.asInt();
		}, AlchemicalBrewing.STONE_CRUCIBLE);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			if (stack.getItem() instanceof FilledVial && tintIndex == 0) {
				return FluidEffectUtil.getColorForStack(stack);
			} else {
				return Color.WHITE.asInt();
			}
		}, AlchemicalBrewing.FILLED_VIAL);
   }
}