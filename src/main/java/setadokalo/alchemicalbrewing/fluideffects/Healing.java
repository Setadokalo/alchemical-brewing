package setadokalo.alchemicalbrewing.fluideffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.apache.commons.math3.fraction.BigFraction;
import setadokalo.alchemicalbrewing.util.Color;

public class Healing extends FluidEffect {
	public final BigFraction healingFactor;
	public static final Color HEALING_COLOR = new Color(255, 20, 20);

	public Healing(ResourceLocation id, BigFraction healingFactor) {
		super(id);
		this.healingFactor = healingFactor;
	}

	public Healing(ResourceLocation id) {
		super(id);
		this.healingFactor = new BigFraction(3, 1);
	}
	
	@Override
	public void applyEffect(Level world, LivingEntity entity, BigFraction fConcentration) {
		entity.heal(fConcentration.multiply(healingFactor).intValue());
	}

	// @Override
	// public Color getColor(ItemStack stack) {
	// 	return HEALING_COLOR;
	// }
}
