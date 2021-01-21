package setadokalo.alchemicalbrewing.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.logging.log4j.Level;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;

public final class AlchemyRecipe {

	public final ConcentratedFluid[] results;
	public final Item[] ingredients;

	protected Identifier identifier;

	public AlchemyRecipe(Identifier id, ConcentratedFluid[] results, Item[] ingredients) {
		this.identifier = id;
		this.results = results;
		this.ingredients = ingredients;
	}

	public Identifier getIdentifier() {
	return identifier;
	}

	private static final String INGR_KEY = "ingredients";
	public static AlchemyRecipe fromJson(Identifier id, JsonObject json) {
		if (!json.has(INGR_KEY) || !json.get(INGR_KEY).isJsonArray()) {
			throw new JsonParseException(
					AlchemicalBrewing.MOD_NAME + " json array requires array with key \"ingredients\".");
		}
		JsonArray ingredientArray = json.getAsJsonArray(INGR_KEY);
		Item[] ingredients = new Item[ingredientArray.size()];
		for (int i = 0; i < ingredients.length; i++) {
			JsonObject ingredientDefinition = ingredientArray.get(i).getAsJsonObject();
			Identifier ingredientId = Identifier.tryParse(ingredientDefinition.get("item").getAsString());
			if (ingredientId == null) {
				throw new JsonParseException("Invalid ingredient item ID in " + id.toString() + " json: "
						+ ingredientArray.get(i).getAsString());
			}
			ingredients[i] = Registry.ITEM.get(ingredientId);
			if (ingredients[i] == null) {
				throw new JsonParseException("Unregistered ingredient item ID in " + id.toString() + " json: "
						+ ingredientId.toString());
			}
		}
		JsonArray resultArray = json.getAsJsonArray("results");
		ConcentratedFluid[] results = new ConcentratedFluid[resultArray.size()];
		for (int i = 0; i < results.length; i++) {
			results[i] = ConcentratedFluid.fromJson(resultArray.get(i).getAsJsonObject());
			if (results[i] == null) {
				throw new JsonParseException(
						"Unregistered result effect in " + id.toString() + " json");
			}
		}
		AlchemicalBrewing.log(Level.INFO, "Adding new recipe with id " + id.toString());
		return new AlchemyRecipe(id, results, ingredients);
	}
}
