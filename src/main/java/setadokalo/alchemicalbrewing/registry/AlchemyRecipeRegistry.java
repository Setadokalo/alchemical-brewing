package setadokalo.alchemicalbrewing.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;

public class AlchemyRecipeRegistry {
	private AlchemyRecipeRegistry() {}

	private static HashMap<Identifier, AlchemyRecipe> idToAlchemyRecipe = new HashMap<>();

	public static AlchemyRecipe register(AlchemyRecipe alchemyRecipe) {
		Identifier id = alchemyRecipe.getIdentifier();
		// if (idToAlchemyRecipe.containsKey(id)) {
		// 	throw new IllegalArgumentException("Duplicate alchemyRecipe id tried to register: '" + id.toString() + "'");
		// }
		idToAlchemyRecipe.put(id, alchemyRecipe);
		return alchemyRecipe;
	}

	protected static AlchemyRecipe update(AlchemyRecipe alchemyRecipe) {
		Identifier id = alchemyRecipe.getIdentifier();
		if (idToAlchemyRecipe.containsKey(id)) {
			idToAlchemyRecipe.remove(id);
		}
		return register(alchemyRecipe);
	}

	public static int size() {
		return idToAlchemyRecipe.size();
	}

	public static Stream<Identifier> identifiers() {
		return idToAlchemyRecipe.keySet().stream();
	}

	public static Iterable<Map.Entry<Identifier, AlchemyRecipe>> entries() {
		return idToAlchemyRecipe.entrySet();
	}

	public static Iterable<AlchemyRecipe> values() {
		return idToAlchemyRecipe.values();
	}

	public static AlchemyRecipe get(Identifier id) {
		if (!idToAlchemyRecipe.containsKey(id)) {
			throw new IllegalArgumentException(
					"Could not get alchemyRecipe from id '" + id.toString() + "', as it was not registered!");
		}
		return idToAlchemyRecipe.get(id);
	}

	public static boolean contains(Identifier id) {
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