package setadokalo.alchemicalbrewing.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.fluideffects.FluidEffect;

public class AlchemyEffectRegistry {

	private AlchemyEffectRegistry() {}

	private static HashMap<Identifier, FluidEffect> idToFluidEffect = new HashMap<>();

	public static FluidEffect register(FluidEffect effect) {
		Identifier id = effect.getIdentifier();
		if (idToFluidEffect.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate alchemyRecipe id tried to register: '" + id.toString() + "'");
		}
		idToFluidEffect.put(id, effect);
		return effect;
	}

	protected static FluidEffect update(FluidEffect effect) {
		Identifier id = effect.getIdentifier();
		if (idToFluidEffect.containsKey(id)) {
			idToFluidEffect.remove(id);
		}
		return register(effect);
	}

	public static int size() {
		return idToFluidEffect.size();
	}

	public static Stream<Identifier> identifiers() {
		return idToFluidEffect.keySet().stream();
	}

	public static Iterable<Map.Entry<Identifier, FluidEffect>> entries() {
		return idToFluidEffect.entrySet();
	}

	public static Iterable<FluidEffect> values() {
		return idToFluidEffect.values();
	}

	public static FluidEffect get(Identifier id) {
		if (!idToFluidEffect.containsKey(id)) {
			throw new IllegalArgumentException(
					"Could not get alchemyRecipe from id '" + id.toString() + "', as it was not registered!");
		}
		return idToFluidEffect.get(id);
	}

	public static boolean contains(Identifier id) {
		return idToFluidEffect.containsKey(id);
	}

	public static boolean contains(FluidEffect effect) {
		return contains(effect.getIdentifier());
	}

	public static void clear() {
		idToFluidEffect.clear();
	}

	public static void reset() {
		clear();
	}
}