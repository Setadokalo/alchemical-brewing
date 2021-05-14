package setadokalo.alchemicalbrewing.fluideffects;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.util.Color;

public interface FluidEffectProvider {
	// ChatFormatting color constants will always return non-null Integers for getColor
	@SuppressWarnings({"ConstantConditions"})
	static abstract class EffectType {
		public static class	POSITIVE extends EffectType {
			public static final EffectType INSTANCE = new POSITIVE();
			protected POSITIVE() {
				super(ChatFormatting.AQUA.getColor());
			}
		}
		public static class NEUTRAL extends EffectType {
			public static final EffectType INSTANCE = new NEUTRAL();
			protected NEUTRAL() {
				super(ChatFormatting.WHITE.getColor());
			}
		}
		public static class NEGATIVE extends EffectType {
			public static final EffectType INSTANCE = new NEGATIVE();
			protected NEGATIVE() {
				super(ChatFormatting.RED.getColor());
			}
		}
		public static class NONE extends EffectType {
			public static final EffectType INSTANCE = new NONE();
			protected NONE() {
				super(ChatFormatting.GRAY.getColor());
			}
		}
		public static class OTHER extends EffectType {
			public OTHER(final int color) {
				super(color);
			}
		}
		public static class RAINBOW extends EffectType {
			public static final EffectType INSTANCE_1SEC = new RAINBOW(1.0);
			protected final double period;
			public RAINBOW(double period) {
				super(0xFFFFFFFF);
				this.period = period * 1000.0;
			}


			protected int getCurrentColor() {
				var curTime = ((double)System.currentTimeMillis()) / period;
				return (Color.sinU8(curTime) << 16)
					+ (Color.sinU8(curTime + Math.PI * 2.0/3.0) << 8)
					+ Color.sinU8(curTime + Math.PI * 4.0/3.0);
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T extends MutableComponent> T format(T component) {
				return (T) component.withStyle(component.getStyle().withColor(getCurrentColor()));
			}
		}


		protected final int color;

		EffectType(final int color) {
			this.color = color;
		}

//		public ChatFormatting getFormatting() {
//			return this.format;
//		}
		@SuppressWarnings("unchecked")
		public <T extends MutableComponent> T format(T component) {
			return (T) component.withStyle(component.getStyle().withColor(color));
		}
	}

	List<FluidEffect> getEffects();
	EffectType getEffectType();
}
