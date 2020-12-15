package setadokalo.alchemicalbrewing.fluideffects;

import java.awt.Color;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class FluidEffect {
	protected String translationKey;
	public FluidEffect(String key) {
		translationKey = key;
	}

	public void applyEffect(World world, Entity entity, double concentration) {
		
	}

	public Color getColor(World world, Entity entity) {
		return Color.WHITE;
	}

	public String getTranslationKey() {
		return this.translationKey;
	}
}
