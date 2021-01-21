package setadokalo.alchemicalbrewing.util;

import java.util.List;

import org.apache.commons.math3.fraction.Fraction;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.fluids.AlchemyFluid;
import setadokalo.alchemicalbrewing.item.FilledVial;

public class FluidEffectUtil {
	private FluidEffectUtil() {}

	public static int getColorForStack(ItemStack stack) {
		if (stack.getItem() == AlchemicalBrewing.FILLED_VIAL) {
			List<ConcentratedFluid> effects = FilledVial.getFluids(stack);
			return getColorForEffects(effects, stack);
		}
		return 0;
	}
	public static int getColorForEffects(List<ConcentratedFluid> effects, @Nullable ItemStack stack) {
		Fraction totalConcentration = Fraction.ZERO;
		for (ConcentratedFluid effect : effects) {
			totalConcentration = totalConcentration.add(effect.concentration);
		}
		Color totalColor = Color.BLACK;
		double dTotalCon = totalConcentration.doubleValue();
		for (ConcentratedFluid effect : effects) {
			Color currentColor = effect.fluid.getColor(stack);
			currentColor = currentColor.mul(effect.concentration.doubleValue() / dTotalCon);
			totalColor = totalColor.add(currentColor);
		}
		if (dTotalCon < 5.0) {
			totalColor = Color.WATER.mix(totalColor, dTotalCon / 5.0);
		}
		return totalColor.asInt();
	}

	public static CompoundTag getFullTagForEffects(ConcentratedFluid... effects) {
		CompoundTag tag = new CompoundTag();
		ListTag lEffects = FilledVial.getTagForFluids(effects);
		tag.put("Effects", lEffects);
		return tag;
	}
	public static ItemStack getDefaultVialForEffects(AlchemyFluid... effects) {
		ItemStack stack = new ItemStack(AlchemicalBrewing.FILLED_VIAL);
		ConcentratedFluid[] cEffects = new ConcentratedFluid[effects.length];
		for (int i = 0; i < effects.length; i++) {
			cEffects[i] = new ConcentratedFluid(effects[i], Fraction.ONE);
		}
		stack.setTag(getFullTagForEffects(cEffects));
		return stack;
	}
}
