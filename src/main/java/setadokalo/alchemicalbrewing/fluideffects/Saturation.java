package setadokalo.alchemicalbrewing.fluideffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.math3.fraction.BigFraction;
import setadokalo.alchemicalbrewing.util.Color;

public class Saturation extends FluidEffect {
	public static final Color STEW_COLOR = new Color(143, 71, 0);
	public final BigFraction hungerFactor; 
	public final float saturationModifier;


	public Saturation(ResourceLocation id, BigFraction hungerFactor, float saturationModifier) {
		super(id);
		this.hungerFactor = hungerFactor;
		this.saturationModifier = saturationModifier;
	}

	public Saturation(ResourceLocation id) {
		super(id);
		this.hungerFactor = new BigFraction(3, 1);
		this.saturationModifier = 0.75f;
	}

	@Override
	public void applyEffect(Level world, LivingEntity entity, BigFraction fConcentration) {
		if (entity instanceof Player) {
			((Player)entity).getFoodData().eat(fConcentration.multiply(hungerFactor).intValue(), saturationModifier);
		}
	}

	// @Override
	// public Color getColor(ItemStack stack) {
	// 	return STEW_COLOR;
	// }

}
