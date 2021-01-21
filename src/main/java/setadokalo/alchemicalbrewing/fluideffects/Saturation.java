package setadokalo.alchemicalbrewing.fluideffects;

import org.apache.commons.math3.fraction.Fraction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.util.Color;

public class Saturation extends FluidEffect {
	public static final Color STEW_COLOR = new Color(143, 71, 0);
	public final Fraction hungerFactor; 
	public final float saturationModifier;


	public Saturation(Identifier id, Fraction hungerFactor, float saturationModifier) {
		super(id);
		this.hungerFactor = hungerFactor;
		this.saturationModifier = saturationModifier;
	}

	public Saturation(Identifier id) {
		super(id);
		this.hungerFactor = new Fraction(3, 1);
		this.saturationModifier = 0.75f;
	}

	@Override
	public void applyEffect(World world, LivingEntity entity, Fraction fConcentration) {
		if (entity instanceof PlayerEntity) {
			((PlayerEntity)entity).getHungerManager().add(fConcentration.multiply(hungerFactor).intValue(), saturationModifier);
		}
	}

	// @Override
	// public Color getColor(ItemStack stack) {
	// 	return STEW_COLOR;
	// }

}
