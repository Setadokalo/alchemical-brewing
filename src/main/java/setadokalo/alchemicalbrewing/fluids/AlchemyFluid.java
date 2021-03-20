package setadokalo.alchemicalbrewing.fluids;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.fluideffects.FluidEffect;
import setadokalo.alchemicalbrewing.fluideffects.FluidEffectProvider;
import setadokalo.alchemicalbrewing.registry.AlchemyFluidRegistry;
import setadokalo.alchemicalbrewing.util.Color;

/**
 * This class will, once complete, be the base class of things like "Stew" or
 * "Healing (fluid)", and `FluidEffect` will become an internal class for
 * representing anything with an effect when drank. `Vials` will contain both
 * `Fluid` and `Item` lists, and both will be kept in sync with a
 * `FluidEffectProvider` list.
 */
public abstract class AlchemyFluid implements FluidEffectProvider {
	private static final String ID = "ID";
	@Nullable
	protected String partialtranslationKey;
	protected EffectType type;
	protected Identifier identifier;

	protected void generateTranslationKey() {
		this.partialtranslationKey = "fluid." + this.identifier.getNamespace() + "." + this.identifier.getPath();
	}

	@Nullable
	public Text getTooltip(String concentration) {
		if (this.partialtranslationKey == null)
			generateTranslationKey();
		return new TranslatableText("tooltip." + this.partialtranslationKey, concentration).formatted(this.type.getFormatting());
	}

	@Nullable
	public Text getName() {
		if (this.partialtranslationKey == null)
			generateTranslationKey();
		return new TranslatableText("name." + this.partialtranslationKey).formatted(this.type.getFormatting());
	}

	/**
	 * Gets the effects for this fluid effect. <br><br>
	 * <b>WARNING FOR IMPLEMENTORS:</b> Be wary about creating your effect lists early (i.e. in the constructor),
	 * as if you attempt to load a FluidEffect before it has been added to the registry undefined behavior will result.
	 */
	@Override
	public List<FluidEffect> getEffects() {
		return new ArrayList<>();
	}

	@Override
	public EffectType getEffectType() {
		return EffectType.NONE;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public Color getColor(@Nullable ItemStack stack) {
		return Color.WATER;
	}

	/** Gets the fluid from an NBT tag.
	 */
	public static AlchemyFluid fromTag(NbtCompound NbtCompound) {
		if (NbtCompound.contains(ID, 8)) {
			String id = NbtCompound.getString(ID);
			return AlchemyFluidRegistry.get(new Identifier(id));
		}
		return null;
	}
	
}
