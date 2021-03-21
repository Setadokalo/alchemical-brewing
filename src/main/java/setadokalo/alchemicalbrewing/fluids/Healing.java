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
		type = EffectType.POSITIVE;
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
		return Color.RED;
	}
}
