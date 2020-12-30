package setadokalo.alchemicalbrewing.reciperegistry;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.apache.logging.log4j.Level;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;
import setadokalo.alchemicalbrewing.registry.AlchemyRecipeRegistry;
import setadokalo.alchemicalbrewing.util.MultiJsonDataLoader;

public class AlchemyRecipeManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public AlchemyRecipeManager() {
		super(GSON, "alchemy");
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(AlchemicalBrewing.MODID, "alchemy");
	}

	@Override
	protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
		AlchemicalBrewing.log(Level.INFO, "Reload detected");
		AlchemyRecipeRegistry.reset();
		loader.forEach((id, jel) ->  // for each json file
			jel.forEach(je -> { // for each root element in those json files
				try {
					AlchemyRecipe recipe = AlchemyRecipe.fromJson(id, je.getAsJsonObject());
					AlchemyRecipeRegistry.register(recipe);
				} catch (Exception e) {
					AlchemicalBrewing.log(Level.INFO, "Error loading alchemy recipe file " + id.toString() + " for reason '" + e.getMessage() + "'");
				}
			})
		);
	}
	
}
