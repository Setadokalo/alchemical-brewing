package setadokalo.alchemicalbrewing.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;

import setadokalo.alchemicalbrewing.AlchemicalBrewing;

@Environment(EnvType.CLIENT)
public class AlchemicalBrewingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(AlchemicalBrewing.STONE_CRUCIBLE, RenderLayer.getTranslucent());
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            assert view != null;
            return BiomeColors.getWaterColor(view, pos);
		  }, AlchemicalBrewing.STONE_CRUCIBLE);
		  
    }
}
