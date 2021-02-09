package setadokalo.alchemicalbrewing;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import setadokalo.alchemicalbrewing.config.ABConfigScreen;

public class AlchemicalBrewingModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<ABConfigScreen> getModConfigScreenFactory() {
		return ABConfigScreen::new;
		
	}
	
}
