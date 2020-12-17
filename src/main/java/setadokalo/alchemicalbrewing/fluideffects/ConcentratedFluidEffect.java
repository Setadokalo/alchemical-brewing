package setadokalo.alchemicalbrewing.fluideffects;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
	public double concentration;

	public ConcentratedFluidEffect(FluidEffect fEffect, double conc) {
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
		if (compoundTag.contains("concentration", 6)) {
			double conc = compoundTag.getDouble("concentration");
			return new ConcentratedFluidEffect(effect, conc);
		} else {
			return new ConcentratedFluidEffect(effect, 1.0);
		}
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from a JSON object.
	 * (Used to create recipe results from datapacks)
	 */
	public static ConcentratedFluidEffect fromJson(JsonObject jObject) {
		FluidEffect effect = AlchemyEffectRegistry.get(Identifier.tryParse(jObject.get("effect").getAsString()));
		double concentration = jObject.get("concentration").getAsDouble();
		return new ConcentratedFluidEffect(effect, concentration);
	}

	public ConcentratedFluidEffect clone() {
		return new ConcentratedFluidEffect(this.effect, this.concentration);
	}

	public void toTag(CompoundTag tag) {
		this.effect.toTag(tag);
		tag.putDouble("concentration", this.concentration);
	}

	public Text getTooltip() {
		return this.effect.getTooltip(this.concentration);
	}

	public ConcentratedFluidEffect split(double fracToTake) {
		if (fracToTake < 0.0 || fracToTake > 1.0)
			throw new IllegalArgumentException("fracToTake must be between 0 and 1");
		ConcentratedFluidEffect newEffect = new ConcentratedFluidEffect(this.effect, this.concentration * fracToTake);
		this.concentration *= (1.0 - fracToTake);
		return newEffect;
	}
}