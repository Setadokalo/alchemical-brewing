package setadokalo.alchemicalbrewing.fluideffects;

import com.mojang.math.Vector3d;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.math3.fraction.BigFraction;

/**
 * Helper class for fluid effects that splash like a vanilla splash potion (by simply splashing entities in an area).
 * Subclasses should not override `applyThrownEffect` and should instead implement `splashTarget`.
 */
public abstract class SplashyFluidEffect extends FluidEffect {
	/**
	 * Constructs a new fluid effect.
	 *
	 * @param id the identifier to refer to this effect with in the registry
	 *           NOTE: `id` should always be unique and the same as the identifier used to
	 *           register this fluid effect in the registry! Undefined behavior will result
	 *           if it is not!
	 */
	protected SplashyFluidEffect(ResourceLocation id) {
		super(id);
	}

	@Override
	public void applyThrownEffect(Level world, Vec3 impactLocation, BigFraction concentration) {
		var splashedEntities = world.getEntities(null, AABB.ofSize(impactLocation, 4.0, 2.0, 4.0));
		for (var entity : splashedEntities) {
			if (!(entity instanceof LivingEntity lE) || !lE.isAffectedByPotions()) continue;
			splashTarget(world, lE, concentration, impactLocation.distanceTo(lE.position()));
		}
	}

	protected abstract void splashTarget(Level world, LivingEntity entity, BigFraction concentration, double distance);
}
