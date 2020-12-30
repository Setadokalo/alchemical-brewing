package setadokalo.alchemicalbrewing.fluideffects;

import org.apache.commons.math3.fraction.Fraction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.util.Color;

public class Stew extends FluidEffect {
	public static final Color STEW_COLOR = new Color(143, 71, 0);
	public static final Fraction STEW_BASE_HUNGER = new Fraction(3, 1); 


	public Stew(Identifier id) {
		super(id, EffectType.POSITIVE);
	}
	
	public Stew(Identifier id, String key) {
		super(id, key, EffectType.POSITIVE);
	}
	
	@Override
	public void applyEffect(World world, Entity entity, Fraction fConcentration) {
		if (entity instanceof PlayerEntity) {
			((PlayerEntity)entity).getHungerManager().add((int)(fConcentration.multiply(STEW_BASE_HUNGER).intValue()), 0.75f);
		}
	}

	@Override
	public Color getColor(ItemStack stack) {
		return STEW_COLOR;
	}
}
