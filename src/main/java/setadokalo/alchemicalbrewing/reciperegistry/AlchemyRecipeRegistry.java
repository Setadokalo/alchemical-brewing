package setadokalo.alchemicalbrewing.reciperegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;

public class AlchemyRecipeRegistry {

	private static HashMap<Identifier, AlchemyRecipe> idToAlchemyRecipe = new HashMap<>();

	public static AlchemyRecipe register(AlchemyRecipe alchemyRecipe) {
		return register(alchemyRecipe.getIdentifier(), alchemyRecipe);
	}

	public static AlchemyRecipe register(Identifier id, AlchemyRecipe alchemyRecipe) {
		if (idToAlchemyRecipe.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate alchemyRecipe id tried to register: '" + id.toString() + "'");
		}
		idToAlchemyRecipe.put(id, alchemyRecipe);
		return alchemyRecipe;
	}

	protected static AlchemyRecipe update(Identifier id, AlchemyRecipe alchemyRecipe) {
		if (idToAlchemyRecipe.containsKey(id)) {
			idToAlchemyRecipe.remove(id);
		}
		return register(id, alchemyRecipe);
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
		AlchemyRecipe alchemyRecipe = idToAlchemyRecipe.get(id);
		return alchemyRecipe;
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
		register(AlchemyRecipe.EMPTY);
	}
}