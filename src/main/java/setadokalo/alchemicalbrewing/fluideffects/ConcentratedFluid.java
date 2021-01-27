package setadokalo.alchemicalbrewing.fluideffects;

import com.google.gson.JsonObject;

import org.apache.commons.math3.fraction.Fraction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.fluids.AlchemyFluid;
import setadokalo.alchemicalbrewing.registry.AlchemyFluidRegistry;
import setadokalo.alchemicalbrewing.util.FractionUtil;

// while a `FluidEffect` instance is a shared instance among all potions
/**
 * A single fluid effect in a container, and it's intensity.
 * Whereas `FluidEffect` is a global class that is shared among all potions, crucibles, etc.,
 * A `ConcentratedFluidEffect` describes the effect in a single container.
 */
public class ConcentratedFluid {
	public AlchemyFluid fluid;
	public Fraction concentration; 

	public ConcentratedFluid(AlchemyFluid fluid, Fraction conc) {
		this.fluid = fluid;
		concentration = conc;
	}

	private static final String CONTAG = "Concentration";
	private static final String FLUIDTAG = "Fluid";


	/**
	 * Serializes a ConcentratedFluid instance to an NBT tag.
	 * (Used to store Effect data in ItemStacks)
	 * @return 
	 */
	public CompoundTag toTag(CompoundTag tag) {
		tag.putString(FLUIDTAG, this.fluid.getIdentifier().toString());
		tag.putString(CONTAG, this.concentration.toString());
		return tag;
	}
	/**
	 * Constructs a ConcentratedFluid instance from an NBT tag.
	 * (Used to get Effect data from ItemStacks)
	 */
	public static ConcentratedFluid fromTag(CompoundTag compoundTag) {
		AlchemyFluid fluid = AlchemyFluidRegistry.get(new Identifier(compoundTag.getString(FLUIDTAG)));
		if (fluid == null) {
			return null;
		}
		if (compoundTag.contains(CONTAG, 8) ) {
			String concentration = compoundTag.getString(CONTAG);
			return new ConcentratedFluid(fluid, FractionUtil.fromString(concentration));
		} else {
			return new ConcentratedFluid(fluid, Fraction.ONE);
		}
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from a JSON object.
	 * (Used to create recipe results from datapacks)
	 */
	public static ConcentratedFluid fromJson(JsonObject jObject) {
		AlchemyFluid fluid = AlchemyFluidRegistry.get(Identifier.tryParse(jObject.get("fluid").getAsString()));
		String concentrationString = jObject.get("concentration").getAsString();
		return new ConcentratedFluid(fluid, FractionUtil.fromString(concentrationString));
	}

	public ConcentratedFluid clone() {
		return new ConcentratedFluid(this.fluid, this.concentration);
	}


	public Text getTooltip() {
		return this.fluid.getTooltip(FractionUtil.toProperString(this.concentration));
	}

	public ConcentratedFluid split(Fraction fracToTake) {
		double dFrac = fracToTake.doubleValue();
		if (dFrac < 0.0 || dFrac > 1.0)
			throw new IllegalArgumentException("fracToTake must be between 0 and 1");
		Fraction removed;
		try {
			removed = this.concentration.multiply(fracToTake);
		} catch (ArithmeticException e) {
			removed = Fraction.ZERO;
		}
		ConcentratedFluid newEffect = new ConcentratedFluid(this.fluid, removed);
		this.concentration = this.concentration.subtract(removed);
		return newEffect;
	}

	public void applyEffects(World world, LivingEntity user) {
		for (FluidEffect effect : this.fluid.getEffects())
			effect.applyEffect(world, user, this.concentration);
	}
}