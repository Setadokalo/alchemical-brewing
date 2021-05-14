package setadokalo.alchemicalbrewing.fluideffects;

import com.mojang.math.Vector3d;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.math3.fraction.BigFraction;
import setadokalo.alchemicalbrewing.util.Color;

public class Saturation extends SplashyFluidEffect {
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
	protected void splashTarget(Level world, LivingEntity entity, BigFraction fConcentration, double distance) {
		if (entity instanceof Player player) {
			player.getFoodData().eat((int) (fConcentration.multiply(hungerFactor).doubleValue() * distance * 0.5), saturationModifier);
		}
	}

	@Override
	public void applyDrinkEffect(Level world, LivingEntity entity, BigFraction fConcentration) {
		if (entity instanceof Player) {
			((Player)entity).getFoodData().eat(fConcentration.multiply(hungerFactor).intValue(), saturationModifier);
		}
	}



}
