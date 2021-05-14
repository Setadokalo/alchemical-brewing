package setadokalo.alchemicalbrewing.fluideffects;

import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.math3.fraction.BigFraction;

/**
 * An effect that can be in a fluid (shocker, I know). 
 */
public abstract class FluidEffect {
	protected ResourceLocation identifier;

	/**
	 * Constructs a new fluid effect.
	 * @param id the identifier to refer to this effect with in the registry
	 * NOTE: `id` should always be unique and the same as the identifier used to
	 * register this fluid effect in the registry! Undefined behavior will result
	 * if it is not!
	 */
	protected FluidEffect(ResourceLocation id) {
		if (id == null) {
			throw new NullPointerException("id must not be null");
		}
		identifier = id;
	}

	/**
	 * Applies the effect of this potion when a user finishes drinking it.
	 * <br><br>
	 * <b>IMPORTANT NOTE:</b> This mod considers an "effect" to be an INSTANTANEOUS effect
	 * at the moment of consumption. To provide a lasting effect, you'll need to 
	 * have `applyEffect` apply a status effect.
	 */
	public abstract void applyDrinkEffect(Level world, LivingEntity entity, BigFraction concentration);

	public abstract void applyThrownEffect(Level world, Vec3 impactLocation, BigFraction concentration);



	public ResourceLocation getIdentifier() {
		return this.identifier;
	}


	private static final String ID = "identifier";


	public void getTag(CompoundTag tag) {
		tag.putString(ID, this.identifier.toString());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof FluidEffect) {
			return (((FluidEffect)object).identifier.equals(this.identifier));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

}
