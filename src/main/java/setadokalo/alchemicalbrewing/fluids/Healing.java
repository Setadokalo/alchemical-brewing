package setadokalo.alchemicalbrewing.fluids;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.FluidEffect;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import setadokalo.alchemicalbrewing.util.Color;

public class Healing extends AlchemyFluid {
	@Nullable
	private List<FluidEffect> lazyEffects = null;
	public Healing(ResourceLocation identifier) {
		type = EffectType.RAINBOW.INSTANCE_1SEC;
		this.identifier = identifier;
	}

	
	@Override
	public List<FluidEffect> getEffects() {
		if (lazyEffects == null) {
			lazyEffects = new ArrayList<>();
			lazyEffects.add(AlchemyEffectRegistry.get(new ResourceLocation(AlchemicalBrewing.MODID, "healing")));
		}
		return lazyEffects;
	}

	@Override
	public Color getColor(@Nullable ItemStack stack) {
		var curTime = ((double)System.currentTimeMillis()) / 1000.0;
		return new Color(Color.sinU8(curTime),
			Color.sinU8(curTime + Math.PI * 2.0/3.0),
			Color.sinU8(curTime + Math.PI * 4.0/3.0)
		);
	}
}
