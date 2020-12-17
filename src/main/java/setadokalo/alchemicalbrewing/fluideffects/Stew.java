package setadokalo.alchemicalbrewing.fluideffects;

import java.awt.Color;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Stew extends FluidEffect {
	public static final Color stewColor = new Color(143, 71, 0);

	public Stew(Identifier id) {
		super(id, EffectType.POSITIVE);
	}
	
	public Stew(Identifier id, String key) {
		super(id, key, EffectType.POSITIVE);
	}
	
	public void applyEffect(World world, Entity entity, double concentration) {
		if (entity instanceof PlayerEntity) {
			((PlayerEntity)entity).getHungerManager().add((int)(7.0 * concentration), 0.75f);
		}
	}

	public Color getColor(World world, Entity entity) {
		return stewColor;
	}
}
