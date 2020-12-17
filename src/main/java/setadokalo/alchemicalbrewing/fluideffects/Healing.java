package setadokalo.alchemicalbrewing.fluideffects;

import org.apache.commons.lang3.math.Fraction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.util.Color;

public class Healing extends FluidEffect {
	public static final Fraction HEALING_BASE = Fraction.getFraction(3, 1);
	public static final Color HEALING_COLOR = new Color(255, 20, 20);

	public Healing(Identifier id) {
		super(id, EffectType.POSITIVE);
	}
	public Healing(Identifier id, String tKey) {
		super(id, tKey, EffectType.POSITIVE);
	}
	
	@Override
	public void applyEffect(World world, Entity entity, Fraction fConcentration) {
		if (entity instanceof LivingEntity) {
			((LivingEntity)entity).heal(fConcentration.multiplyBy(HEALING_BASE).getProperWhole());
		}
	}

	@Override
	public Color getColor(ItemStack stack) {
		return HEALING_COLOR;
	}
}
