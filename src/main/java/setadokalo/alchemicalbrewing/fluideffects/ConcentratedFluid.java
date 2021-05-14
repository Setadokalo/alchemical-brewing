package setadokalo.alchemicalbrewing.fluideffects;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.Nullable;
import setadokalo.alchemicalbrewing.fluids.AlchemyFluid;
import setadokalo.alchemicalbrewing.registry.AlchemyFluidRegistry;
import setadokalo.alchemicalbrewing.util.Color;
import setadokalo.alchemicalbrewing.util.BigFractionUtil;

// while a `FluidEffect` instance is a shared instance among all potions
/**
 * A single fluid effect in a container, and it's intensity.
 * Whereas `FluidEffect` is a global class that is shared among all potions, crucibles, etc.,
 * A `ConcentratedFluidEffect` describes the effect in a single container.
 */
public class ConcentratedFluid {
	public AlchemyFluid fluid;
	public BigFraction concentration; 

	public ConcentratedFluid(AlchemyFluid fluid, BigFraction conc) {
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
	public CompoundTag writeNbt(CompoundTag tag) {
		tag.putString(FLUIDTAG, this.fluid.getIdentifier().toString());
		tag.putString(CONTAG, this.concentration.toString());
		return tag;
	}
	/**
	 * Constructs a ConcentratedFluid instance from an NBT tag.
	 * (Used to get Effect data from ItemStacks)
	 */
	public static ConcentratedFluid fromTag(CompoundTag NbtCompound) {
		AlchemyFluid fluid = AlchemyFluidRegistry.get(new ResourceLocation(NbtCompound.getString(FLUIDTAG)));
		if (fluid == null) {
			return null;
		}
		if (NbtCompound.contains(CONTAG, 8) ) {
			String concentration = NbtCompound.getString(CONTAG);
			return new ConcentratedFluid(fluid, BigFractionUtil.fromString(concentration));
		} else {
			return new ConcentratedFluid(fluid, BigFraction.ONE);
		}
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from a JSON object.
	 * (Used to create recipe results from datapacks)
	 */
	public static ConcentratedFluid fromJson(JsonObject jObject) {
		AlchemyFluid fluid = AlchemyFluidRegistry.get(ResourceLocation.tryParse(jObject.get("fluid").getAsString()));
		String concentrationString = jObject.get("concentration").getAsString();
		return new ConcentratedFluid(fluid, BigFractionUtil.fromString(concentrationString));
	}

	public ConcentratedFluid clone() {
		return new ConcentratedFluid(this.fluid, this.concentration);
	}


	public Component getTooltip() {
		return this.fluid.getTooltip(BigFractionUtil.toProperString(this.concentration));
	}

	public ConcentratedFluid split(BigFraction fracToTake) {
		double dFrac = fracToTake.doubleValue();
		if (dFrac < 0.0 || dFrac > 1.0)
			throw new IllegalArgumentException("fracToTake must be between 0 and 1");
		BigFraction removed;
		removed = this.concentration.multiply(fracToTake);
		ConcentratedFluid newEffect = new ConcentratedFluid(this.fluid, removed);
		this.concentration = this.concentration.subtract(removed);
		return newEffect;
	}

	public void applyEffects(Level world, LivingEntity user) {
		for (FluidEffect effect : this.fluid.getEffects())
			effect.applyDrinkEffect(world, user, this.concentration);
	}
	public Color getColor(@Nullable ItemStack stack) {
		return this.fluid.getColor(stack);
	}
}