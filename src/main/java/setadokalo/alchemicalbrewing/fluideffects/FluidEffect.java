package setadokalo.alchemicalbrewing.fluideffects;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import setadokalo.alchemicalbrewing.util.Color;

/**
 * An effect that can be in a fluid (shocker, I know). 
 */
public class FluidEffect {
	public enum EffectType {
		POSITIVE(Formatting.AQUA),
		NEUTRAL(Formatting.WHITE),
		NEGATIVE(Formatting.RED),
		OTHER(Formatting.YELLOW);
		
		private final Formatting format;
		EffectType(Formatting formatting) {
			format = formatting;
		}

		public Formatting getFormatting() {
			return this.format;
		}
	}

	public static final FluidEffect EMPTY = new FluidEffect(new Identifier(AlchemicalBrewing.MODID, "empty"), EffectType.OTHER);
	@Nullable
	protected String partialtranslationKey;
	protected EffectType type;
	protected Identifier identifier;

	/**
	 * Constructs a new fluid effect.
	 * @param id the identifier to refer to this effect with in the registry
	 * @param partialKey the part of the translation key after `name` or `tooltip`
	 */
	public FluidEffect(Identifier id, @Nullable String partialKey, EffectType type) {
		assert id != null;
		identifier = id;
		partialtranslationKey = partialKey;
		assert type != null;
		this.type = type;
	}

	public FluidEffect(Identifier id, EffectType type) {
		identifier = id;
		partialtranslationKey = null;
	}

	/**
	 * Applies the effect of this potion when a user finishes drinking it.
	 * <br><br>
	 * <b>IMPORTANT NOTE:</b> This mod considers an "effect" to be an INSTANTANEOUS effect
	 * at the moment of consumption. To provide a lasting effect, you'll need to 
	 * have `applyEffect` apply a STATUS effect.
	 */
	public void applyEffect(World world, Entity entity, Fraction concentration) {
	}

	public Color getColor(@Nullable ItemStack stack) {
		return Color.WHITE;
	}

	protected void generateTranslationKey() {
		this.partialtranslationKey = "fluideffect." + this.identifier.getNamespace() + "." + this.identifier.getPath();
	}

	@Nullable
	public Text getTooltip(String concentration) {
		if (this.partialtranslationKey == null)
			generateTranslationKey();
		return new TranslatableText("tooltip." + this.partialtranslationKey, concentration).formatted(this.type.getFormatting());
	}

	@Nullable
	public Text getName() {
		return new TranslatableText("name." + this.partialtranslationKey).formatted(this.type.getFormatting());
	}

	public Identifier getIdentifier() {
		return this.identifier;
	}

	/**
	 * Constructs a `ConcentratedFluidEffect` instance from an NBT tag.
	 * (Used to get Effect data from `ItemStack`s)
	 */
	public static FluidEffect fromTag(CompoundTag compoundTag) {
		if (compoundTag.contains("identifier", 8)) {
			String id = compoundTag.getString("identifier");
			return AlchemyEffectRegistry.get(new Identifier(id));
		}
		return null;
	}

	public void toTag(CompoundTag tag) {
		tag.putString("identifier", this.identifier.toString());
	}
}
