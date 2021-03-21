package setadokalo.alchemicalbrewing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import setadokalo.alchemicalbrewing.item.ABItems;
import setadokalo.alchemicalbrewing.registry.AlchemyRecipeManager;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import setadokalo.alchemicalbrewing.registry.AlchemyFluidRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import setadokalo.alchemicalbrewing.blocks.Crucible;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;
import setadokalo.alchemicalbrewing.config.ABConfig;
import setadokalo.alchemicalbrewing.fluideffects.Healing;
import setadokalo.alchemicalbrewing.fluideffects.Saturation;

public class AlchemicalBrewing implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "alchemicalbrewing";
	public static final String MOD_NAME = "Alchemical Brewing";
	private static final String LOG_STRING = "[" + MOD_NAME + "] {}";

	public static ABConfig config = ABConfig.getConfig();

	public static final Block STONE_CRUCIBLE = new Crucible();
	public static BlockEntityType<CrucibleEntity> crucibleBlockEntity;

	public static final CreativeModeTab ITEM_GROUP = FabricItemGroupBuilder.create(
		new ResourceLocation(MODID, "general"))
		.icon(() -> new ItemStack(STONE_CRUCIBLE))
		.appendItems(stacks -> {
			stacks.add(new ItemStack(STONE_CRUCIBLE));
			stacks.add(new ItemStack(ABItems.VIAL));
			stacks.add(new ItemStack(ABItems.FILLED_VIAL));
			stacks.add(new ItemStack(ABItems.MORTAR_AND_PESTLE));
			stacks.add(new ItemStack(ABItems.HEALING_ESSENCE));
			stacks.add(new ItemStack(ABItems.PURITY_ESSENCE));
		})
		.build();
	
	
	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new AlchemyRecipeManager());
		
		Registry.register(Registry.BLOCK, new ResourceLocation(MODID, "stone_crucible"), STONE_CRUCIBLE);
		// Builder<CrucibleEntity> builder = ;
		crucibleBlockEntity = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			MODID + ":stone_crucible",
			FabricBlockEntityTypeBuilder.create(CrucibleEntity::new, STONE_CRUCIBLE).build(null)
		);
		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "stone_crucible"),
			new BlockItem(STONE_CRUCIBLE, new Item.Properties().tab(AlchemicalBrewing.ITEM_GROUP))
		);
		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "vial"),
			ABItems.VIAL
		);
		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "filled_vial"),
			ABItems.FILLED_VIAL
		);

		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "mortar_and_pestle"),
			ABItems.MORTAR_AND_PESTLE
		);

		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "healing_essence"),
			ABItems.HEALING_ESSENCE
		);

		Registry.register(
			Registry.ITEM,
			new ResourceLocation(MODID, "purity_essence"),
			ABItems.PURITY_ESSENCE
		);

		AlchemyEffectRegistry.register(new Saturation(new ResourceLocation(MODID, "stew")));
		AlchemyEffectRegistry.register(new Healing(new ResourceLocation(MODID, "healing")));
		//TODO: Rename the effect or fluid
		AlchemyFluidRegistry.register(new setadokalo.alchemicalbrewing.fluids.Healing(new ResourceLocation(MODID, "healing")));
		AlchemyFluidRegistry.register(new setadokalo.alchemicalbrewing.fluids.Stew(new ResourceLocation(MODID, "stew")));
	}
	public static void log(Level level, String message){
		LOGGER.log(level, LOG_STRING, message);
	}
}
