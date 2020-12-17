package setadokalo.alchemicalbrewing.util;

import java.util.List;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluidEffect;
import setadokalo.alchemicalbrewing.item.FilledVial;

public class FluidEffectUtil {
	public static int getColorForStack(ItemStack stack) {
		if (stack.getItem() == AlchemicalBrewing.FILLED_VIAL) {
			List<ConcentratedFluidEffect> effects = FilledVial.getEffects(stack);
			// colors.add(Color.WATER);
			return getColorForEffects(effects, stack);
		}
		return 0;
	}
	public static int getColorForEffects(List<ConcentratedFluidEffect> effects, @Nullable ItemStack stack) {
		Fraction totalConcentration = Fraction.ZERO;
		for (ConcentratedFluidEffect effect : effects) {
			totalConcentration = totalConcentration.add(effect.concentration);
		}
		Color totalColor = Color.BLACK;
		double dTotalCon = totalConcentration.doubleValue();
		for (ConcentratedFluidEffect effect : effects) {
			Color currentColor = effect.effect.getColor(stack);
			currentColor = currentColor.mul(effect.concentration.doubleValue() / dTotalCon);
			totalColor = totalColor.add(currentColor);
		}
		if (dTotalCon < 1.0) {
			totalColor = Color.WATER.mix(totalColor, dTotalCon);
		}
		return totalColor.asInt();
	}
}
