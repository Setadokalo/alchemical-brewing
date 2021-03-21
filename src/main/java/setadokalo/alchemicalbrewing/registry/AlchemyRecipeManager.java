package setadokalo.alchemicalbrewing.registry;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import org.apache.logging.log4j.Level;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;
import setadokalo.alchemicalbrewing.util.MultiJsonDataLoader;

public class AlchemyRecipeManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public AlchemyRecipeManager() {
		super(GSON, "alchemy");
	}

	@Override
	public ResourceLocation getFabricId() {
		return new ResourceLocation(AlchemicalBrewing.MODID, "alchemy");
	}

	/** Takes the list of loaders and processes them
	 */
	@Override
	protected void apply(Map<ResourceLocation, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
		AlchemicalBrewing.log(Level.INFO, "Reload detected");
		AlchemyRecipeRegistry.reset();
		loader.forEach((id, jel) ->  // for each json file
			jel.forEach(je -> { // for each root element in those json files
				try {
					AlchemyRecipe recipe = AlchemyRecipe.fromJson(id, je.getAsJsonObject());
					AlchemyRecipeRegistry.register(recipe);
				} catch (Exception e) {
					AlchemicalBrewing.log(Level.ERROR, "Error loading alchemy recipe file " + id.toString() + " for reason '" + e.getMessage() + "'");
					for (StackTraceElement stackTraceLine: e.getStackTrace()) {
						AlchemicalBrewing.log(Level.ERROR, stackTraceLine.toString());
					}
				}
			})
		);
	}
	
}
