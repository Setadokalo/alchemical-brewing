package setadokalo.alchemicalbrewing.fluideffects;

import org.apache.commons.math3.fraction.Fraction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.util.Color;

public class Healing extends FluidEffect {
	public final Fraction healingFactor;
	public static final Color HEALING_COLOR = new Color(255, 20, 20);

	public Healing(Identifier id, Fraction healingFactor) {
		super(id);
		this.healingFactor = healingFactor;
	}

	public Healing(Identifier id) {
		super(id);
		this.healingFactor = new Fraction(3, 1);
	}
	
	@Override
	public void applyEffect(World world, LivingEntity entity, Fraction fConcentration) {
		entity.heal(fConcentration.multiply(healingFactor).intValue());
	}

	// @Override
	// public Color getColor(ItemStack stack) {
	// 	return HEALING_COLOR;
	// }
}
