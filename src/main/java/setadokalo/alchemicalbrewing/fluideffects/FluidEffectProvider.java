package setadokalo.alchemicalbrewing.fluideffects;

import java.util.List;
import net.minecraft.ChatFormatting;

public interface FluidEffectProvider {
	public enum EffectType {
		POSITIVE(ChatFormatting.AQUA),
		NEUTRAL(ChatFormatting.WHITE),
		NEGATIVE(ChatFormatting.RED),
		NONE(ChatFormatting.GRAY),
		OTHER(ChatFormatting.YELLOW);
		
		private final ChatFormatting format;
		EffectType(ChatFormatting formatting) {
			format = formatting;
		}

		public ChatFormatting getFormatting() {
			return this.format;
		}
	}

	public List<FluidEffect> getEffects();
	public EffectType getEffectType();
}
