package setadokalo.alchemicalbrewing.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import setadokalo.alchemicalbrewing.item.ABItems;

public class VialEntity extends ThrowableItemProjectile {
	public VialEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
		super(entityType, level);
	}

	public VialEntity(EntityType<? extends ThrowableItemProjectile> entityType, double d, double e, double f, Level level) {
		super(entityType, d, e, f, level);
	}

	public VialEntity(EntityType<? extends ThrowableItemProjectile> entityType, LivingEntity livingEntity, Level level) {
		super(entityType, livingEntity, level);
	}

	@Override
	protected Item getDefaultItem() {
		return ABItems.FILLED_VIAL;
	}
}
