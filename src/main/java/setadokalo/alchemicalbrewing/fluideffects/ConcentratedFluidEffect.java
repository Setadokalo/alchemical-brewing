package setadokalo.alchemicalbrewing.fluideffects;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.math.Fraction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;

//TODO: Rename this to better reflect that this is an /instance/ of a fluid effect
// while a `FluidEffect` instance is a shared instance among all potions
/**
 * A single fluid effect in a container, and it's intensity.
 * Whereas `FluidEffect` is a global class that is shared among all potions, crucibles, etc.,
 * A `ConcentratedFluidEffect` describes the effect in a single container.
 */
public class ConcentratedFluidEffect {
	public FluidEffect effect;
	public Fraction concentration; 

	public ConcentratedFluidEffect(FluidEffect fEffect, Fraction conc) {
		effect = fEffect;
		concentration = conc;
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from an NBT tag.
	 * (Used to get Effect data from `ItemStack`s)
	 */
	public static ConcentratedFluidEffect fromTag(CompoundTag compoundTag) {
		FluidEffect effect = FluidEffect.fromTag(compoundTag);
		if (effect == null) {
			return null;
		}
		if (compoundTag.contains("Concentration", 8) ) {
			String concentration = compoundTag.getString("Concentration");
			return new ConcentratedFluidEffect(effect, Fraction.getFraction(concentration));
		} else {
			return new ConcentratedFluidEffect(effect, Fraction.getFraction(1, 1));
		}
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from a JSON object.
	 * (Used to create recipe results from datapacks)
	 */
	public static ConcentratedFluidEffect fromJson(JsonObject jObject) {
		FluidEffect effect = AlchemyEffectRegistry.get(Identifier.tryParse(jObject.get("effect").getAsString()));
		String concentrationString = jObject.get("concentration").getAsString();
		return new ConcentratedFluidEffect(effect, Fraction.getFraction(concentrationString));
	}

	public ConcentratedFluidEffect clone() {
		return new ConcentratedFluidEffect(this.effect, this.concentration);
	}

	public void toTag(CompoundTag tag) {
		this.effect.toTag(tag);
		tag.putString("Concentration", this.concentration.toString());
	}

	public Text getTooltip() {
		return this.effect.getTooltip(this.concentration.toProperString());
	}

	public ConcentratedFluidEffect split(Fraction fracToTake) {
		double dFrac = fracToTake.doubleValue();
		if (dFrac < 0.0 || dFrac > 1.0)
			throw new IllegalArgumentException("fracToTake must be between 0 and 1");
		Fraction removed = this.concentration.multiplyBy(fracToTake);
		ConcentratedFluidEffect newEffect = new ConcentratedFluidEffect(this.effect, removed);
		this.concentration = this.concentration.subtract(removed);
		return newEffect;
	}
}