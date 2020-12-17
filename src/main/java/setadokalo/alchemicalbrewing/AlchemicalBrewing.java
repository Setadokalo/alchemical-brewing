package setadokalo.alchemicalbrewing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import setadokalo.alchemicalbrewing.item.FilledVial;
import setadokalo.alchemicalbrewing.item.Vial;
import setadokalo.alchemicalbrewing.reciperegistry.AlchemyRecipeManager;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import setadokalo.alchemicalbrewing.blocks.Crucible;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;
import setadokalo.alchemicalbrewing.fluideffects.Healing;
import setadokalo.alchemicalbrewing.fluideffects.Stew;

public class AlchemicalBrewing implements ModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "alchemicalbrewing";
	public static final String MOD_NAME = "Alchemical Brewing";
	private static final String LOG_NAME = "[" + MOD_NAME + "] {0}";

	public static final Block STONE_CRUCIBLE = new Crucible();
	public static BlockEntityType<CrucibleEntity> crucibleBlockEntity;
	
	public static final Item VIAL = new Vial();
	public static final Item FILLED_VIAL = new FilledVial();

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
		new Identifier(MODID, "general"))
		.icon(() -> new ItemStack(STONE_CRUCIBLE))
		.appendItems(stacks -> {
			stacks.add(new ItemStack(STONE_CRUCIBLE));
		}).build();
	
	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AlchemyRecipeManager());
		
		Registry.register(Registry.BLOCK, new Identifier(MODID, "stone_crucible"), STONE_CRUCIBLE);
		crucibleBlockEntity = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			MODID + ":stone_crucible",
			BlockEntityType.Builder.create(CrucibleEntity::new, STONE_CRUCIBLE).build(null)
		);
		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "stone_crucible"),
			new BlockItem(STONE_CRUCIBLE, new Item.Settings().group(ItemGroup.BREWING))
		);
		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "vial"),
			VIAL
		);
		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "filled_vial"),
			FILLED_VIAL
		);

		AlchemyEffectRegistry.register(new Stew(new Identifier(MODID, "stew"), "fluideffect." + MODID + ".stew"));
		AlchemyEffectRegistry.register(new Healing(new Identifier(MODID, "healing"), "fluideffect." + MODID + ".healing"));
	}
	public static void log(Level level, String message){
		LOGGER.log(level, LOG_NAME, message);
	}
}
