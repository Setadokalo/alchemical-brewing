package setadokalo.alchemicalbrewing.fluideffects;

import java.util.List;

import net.minecraft.util.Formatting;

public interface FluidEffectProvider {
	public enum EffectType {
		POSITIVE(Formatting.AQUA),
		NEUTRAL(Formatting.WHITE),
		NEGATIVE(Formatting.RED),
		NONE(Formatting.GRAY),
		OTHER(Formatting.YELLOW);
		
		private final Formatting format;
		EffectType(Formatting formatting) {
			format = formatting;
		}

		public Formatting getFormatting() {
			return this.format;
		}
	}

	public List<FluidEffect> getEffects();
	public EffectType getEffectType();
}
