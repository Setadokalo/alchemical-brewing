package setadokalo.alchemicalbrewing.fluideffects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import setadokalo.alchemicalbrewing.util.Color;

/**
 * An effect that can be in a fluid (shocker, I know). 
 */
public class FluidEffect {
	public static final FluidEffect EMPTY = new FluidEffect(new Identifier(AlchemicalBrewing.MODID, "empty"));
	@Nullable
	protected String translationKey;
	protected Identifier identifier;
	protected float concentration;

	public FluidEffect(Identifier id, String key) {
		identifier = id;
		translationKey = key;
	}

	public FluidEffect(Identifier id) {
		identifier = id;
		translationKey = null;
	}

	public void applyEffect(World world, Entity entity, double concentration) {
		// no default effect, for now at least
	}

	public Color getColor(World world, Entity entity, ItemStack stack) {
		return Color.WHITE;
	}

	@Nullable
	public String getTranslationKey() {
		return this.translationKey;
	}

	public Identifier getIdentifier() {
		return this.identifier;
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from an NBT tag.
	 * (Used to get Effect data from `ItemStack`s)
	 */
	public static FluidEffect fromTag(CompoundTag compoundTag) {
		if (compoundTag.contains("id", 8)) {
			String id = compoundTag.getString("id");
			return AlchemyEffectRegistry.get(new Identifier(id));
		}
		return null;
	}
}
