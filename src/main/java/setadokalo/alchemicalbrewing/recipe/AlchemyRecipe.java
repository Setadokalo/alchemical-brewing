package setadokalo.alchemicalbrewing.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;

public final class AlchemyRecipe {

	public final ConcentratedFluid[] results;
	public final ItemPredicate[] ingredients;

	protected ResourceLocation identifier;

	public AlchemyRecipe(ResourceLocation id, ConcentratedFluid[] results, ItemPredicate[] ingredients) {
		this.identifier = id;
		this.results = results;
		this.ingredients = ingredients;
	}

	public ResourceLocation getIdentifier() {
	return identifier;
	}

	private static final String INGR_KEY = "ingredients";
	public static AlchemyRecipe fromJson(ResourceLocation id, JsonObject json) {
		if (!json.has(INGR_KEY) || !json.get(INGR_KEY).isJsonArray()) {
			throw new JsonParseException(
					AlchemicalBrewing.MOD_NAME + " json array requires array with key \"ingredients\".");
		}
		JsonArray ingredientArray = json.getAsJsonArray(INGR_KEY);
		ItemPredicate[] ingredients = new ItemPredicate[ingredientArray.size()];
		for (int i = 0; i < ingredients.length; i++) {
			JsonObject ingredientDefinition = ingredientArray.get(i).getAsJsonObject();
			ingredients[i] = ItemPredicate.fromJson(ingredientDefinition);
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
