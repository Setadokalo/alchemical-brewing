package setadokalo.alchemicalbrewing.util;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.Nullable;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.fluids.AlchemyFluid;
import setadokalo.alchemicalbrewing.item.ABItems;
import setadokalo.alchemicalbrewing.item.FilledVial;

public class FluidEffectUtil {
	private FluidEffectUtil() {}

	public static int getColorForStack(ItemStack stack) {
		if (stack.getItem() == ABItems.FILLED_VIAL) {
			List<ConcentratedFluid> effects = FilledVial.getFluids(stack);
			return getColorForEffects(9000, effects, stack);
		}
		return 0;
	}

	/**
	 * 
	 * @param fluidAmount the amount of water the fluids are dissolved in (using the Fabric API standard,
	 *  where 81000 = 1 Bucket of Water)
	 * @param effects
	 * @param stack
	 * @return
	 */
	public static int getColorForEffects(int fluidAmount, List<ConcentratedFluid> effects, @Nullable ItemStack stack) {
		BigFraction totalConcentration = BigFraction.ZERO;
		for (ConcentratedFluid effect : effects) {
			totalConcentration = totalConcentration.add(effect.concentration);
		}
		Color totalColor = Color.BLACK;
		double dTotalCon = totalConcentration.doubleValue();
		for (ConcentratedFluid effect : effects) {
			Color currentColor = effect.getColor(stack);
			currentColor = currentColor.mul(effect.concentration.doubleValue() / dTotalCon);
			totalColor = totalColor.add(currentColor);
		}
		double conScale = ((double) fluidAmount) / 9000;
		dTotalCon /= conScale;

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
		ItemStack stack = new ItemStack(ABItems.FILLED_VIAL);
		ConcentratedFluid[] cEffects = new ConcentratedFluid[effects.length];
		for (int i = 0; i < effects.length; i++) {
			cEffects[i] = new ConcentratedFluid(effects[i], BigFraction.ONE);
		}
		stack.setTag(getFullTagForEffects(cEffects));
		return stack;
	}
}
