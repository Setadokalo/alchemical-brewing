package setadokalo.alchemicalbrewing.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import org.apache.logging.log4j.Level;

import net.fabricmc.loader.api.FabricLoader;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class ABConfig {
	public enum TooltipMode {
		CIRCLES,
		BAR
	}
	// the .toml file this config was loaded from (and will save itself back to)
	private transient File file = null;
	public TooltipMode tooltipMode = TooltipMode.BAR;
	public static ABConfig getConfig() {
		AlchemicalBrewing.log(Level.INFO, "Loading config file");
		File file = new File(FabricLoader.getInstance().getConfigDir().toString(), AlchemicalBrewing.MODID + ".toml");
		if (file.exists()) {
			Toml configToml = new Toml().read(file);
			ABConfig config = configToml.to(ABConfig.class);
			config.file = file;
			return config;
		} else {
			ABConfig config = new ABConfig();
			config.file = file;
			config.saveConfig();
			return config;
		}
	}

	public void saveConfig() {
		TomlWriter tWr = new TomlWriter();
		try {
			tWr.write(this, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
