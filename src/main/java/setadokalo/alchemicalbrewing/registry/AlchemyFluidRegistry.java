package setadokalo.alchemicalbrewing.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import setadokalo.alchemicalbrewing.fluids.AlchemyFluid;

public class AlchemyFluidRegistry {
	private AlchemyFluidRegistry() {
	}

	private static HashMap<ResourceLocation, AlchemyFluid> idToFluidEffect = new HashMap<>();

	public static AlchemyFluid register(AlchemyFluid alchemyRecipe) {
		ResourceLocation id = alchemyRecipe.getIdentifier();
		if (idToFluidEffect.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate alchemyRecipe id tried to register: '" + id.toString() + "'");
		}
		idToFluidEffect.put(id, alchemyRecipe);
		return alchemyRecipe;
	}

	protected static AlchemyFluid update(AlchemyFluid alchemyRecipe) {
		ResourceLocation id = alchemyRecipe.getIdentifier();
		if (idToFluidEffect.containsKey(id)) {
			idToFluidEffect.remove(id);
		}
		return register(alchemyRecipe);
	}

	public static int size() {
		return idToFluidEffect.size();
	}

	public static Stream<ResourceLocation> identifiers() {
		return idToFluidEffect.keySet().stream();
	}

	public static Iterable<Map.Entry<ResourceLocation, AlchemyFluid>> entries() {
		return idToFluidEffect.entrySet();
	}

	public static Iterable<AlchemyFluid> values() {
		return idToFluidEffect.values();
	}

	public static AlchemyFluid get(ResourceLocation id) {
		if (!idToFluidEffect.containsKey(id)) {
			throw new IllegalArgumentException(
					"Could not get alchemyRecipe from id '" + id.toString() + "', as it was not registered!");
		}
		return idToFluidEffect.get(id);
	}

	public static boolean contains(ResourceLocation id) {
		return idToFluidEffect.containsKey(id);
	}

	public static boolean contains(AlchemyFluid alchemyRecipe) {
		return contains(alchemyRecipe.getIdentifier());
	}

	public static void clear() {
		idToFluidEffect.clear();
	}

	public static void reset() {
		clear();
	}
}
