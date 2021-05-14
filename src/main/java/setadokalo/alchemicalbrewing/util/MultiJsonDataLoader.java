package setadokalo.alchemicalbrewing.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

/** Helper class to simplify the process of loading all JSON files.
 * `prepare` takes the list of JSON files as input and calles 
 * `tryParseJson` on each after sanitizing them,
 */
public abstract class MultiJsonDataLoader
		extends SimplePreparableReloadListener<Map<ResourceLocation, List<JsonElement>>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int FILE_SUFFIX_LENGTH = ".json".length();
	private final Gson gson;
	private final String dataType;

	protected MultiJsonDataLoader(Gson gson, String dataType) {
		this.gson = gson;
		this.dataType = dataType;
	}

	@Override
	protected Map<ResourceLocation, List<JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, List<JsonElement>> map = Maps.newHashMap();
		int i = this.dataType.length() + 1;
		Iterator<ResourceLocation> resources = resourceManager.listResources(this.dataType, path -> path.endsWith(".json")).iterator();
		Set<String> resourcesHandled = new HashSet<>();
		while (resources.hasNext()) {
			ResourceLocation idWithSuffix = resources.next();
			String pathWithSuffix = idWithSuffix.getPath();
			ResourceLocation id = new ResourceLocation(idWithSuffix.getNamespace(),
					pathWithSuffix.substring(i, pathWithSuffix.length() - FILE_SUFFIX_LENGTH));
			resourcesHandled.clear();
			try {
				resourceManager.getResources(idWithSuffix).forEach(resource -> {
					// if this data/resourcepack hasn't already been handled, process it now
					if (!resourcesHandled.contains(resource.getSourceName())) {
						resourcesHandled.add(resource.getSourceName());
						try (
							InputStream inputStream = resource.getInputStream();
							Reader reader = new BufferedReader(
								new InputStreamReader(inputStream, StandardCharsets.UTF_8)
							)
						) {
							parseJson(map, id, reader);
						} catch (IllegalArgumentException | IOException | JsonParseException var68) {
							LOGGER.error("Couldn't parse data file {} from {}", id, idWithSuffix, var68);
						}
					}
				});
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", id, idWithSuffix, e);
			}
		}

		return map;
	}

	/**
	 * Parses a json file into the map.
	 * @param map
	 * @param id the id to associate this json list with; generally the file name without the suffix.
	 * @param reader a reader handle for the file
	 */
	private void parseJson(Map<ResourceLocation, List<JsonElement>> map, ResourceLocation id, Reader reader) {
		JsonElement jsonElement = GsonHelper.fromJson(this.gson, reader,
				JsonElement.class);
		if (jsonElement != null) {
			if (map.containsKey(id)) {
				map.get(id).add(jsonElement);
			} else {
				List<JsonElement> elementList = new LinkedList<>();
				elementList.add(jsonElement);
				map.put(id, elementList);
			}
		} else {
			LOGGER.error("Couldn't load data file {} as it's null or empty", id);
		}
	}
}
