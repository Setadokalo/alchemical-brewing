package setadokalo.alchemicalbrewing.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;

public class AlchemyRecipeRegistry {
	private AlchemyRecipeRegistry() {}

	private static final HashMap<ResourceLocation, AlchemyRecipe> idToAlchemyRecipe = new HashMap<>();

	public static AlchemyRecipe register(AlchemyRecipe alchemyRecipe) {
		ResourceLocation id = alchemyRecipe.getIdentifier();
		idToAlchemyRecipe.put(id, alchemyRecipe);
		return alchemyRecipe;
	}

	protected static AlchemyRecipe update(AlchemyRecipe alchemyRecipe) {
		ResourceLocation id = alchemyRecipe.getIdentifier();
		idToAlchemyRecipe.remove(id);
		return register(alchemyRecipe);
	}

	public static int size() {
		return idToAlchemyRecipe.size();
	}

	public static Stream<ResourceLocation> identifiers() {
		return idToAlchemyRecipe.keySet().stream();
	}

	public static Iterable<Map.Entry<ResourceLocation, AlchemyRecipe>> entries() {
		return idToAlchemyRecipe.entrySet();
	}

	public static Iterable<AlchemyRecipe> values() {
		return idToAlchemyRecipe.values();
	}

	public static AlchemyRecipe get(ResourceLocation id) {
		if (!idToAlchemyRecipe.containsKey(id)) {
			throw new IllegalArgumentException(
					"Could not get alchemyRecipe from id '" + id.toString() + "', as it was not registered!");
		}
		return idToAlchemyRecipe.get(id);
	}

	public static boolean contains(ResourceLocation id) {
		return idToAlchemyRecipe.containsKey(id);
	}

	public static boolean contains(AlchemyRecipe alchemyRecipe) {
		return contains(alchemyRecipe.getIdentifier());
	}

	public static void clear() {
		idToAlchemyRecipe.clear();
	}

	public static void reset() {
		clear();
	}
}