package setadokalo.alchemicalbrewing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import setadokalo.alchemicalbrewing.item.Essence;
import setadokalo.alchemicalbrewing.item.FilledVial;
import setadokalo.alchemicalbrewing.item.MortarAndPestle;
import setadokalo.alchemicalbrewing.item.Vial;
import setadokalo.alchemicalbrewing.registry.AlchemyRecipeManager;
import setadokalo.alchemicalbrewing.registry.AlchemyEffectRegistry;
import setadokalo.alchemicalbrewing.registry.AlchemyFluidRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import setadokalo.alchemicalbrewing.fluideffects.Saturation;

public class AlchemicalBrewing implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "alchemicalbrewing";
	public static final String MOD_NAME = "Alchemical Brewing";
	private static final String LOG_STRING = "[" + MOD_NAME + "] {}";

	public static final Block STONE_CRUCIBLE = new Crucible();
	public static BlockEntityType<CrucibleEntity> crucibleBlockEntity;
	
	public static final Item VIAL = new Vial();
	public static final Item FILLED_VIAL = new FilledVial();
	public static final Item MORTAR_AND_PESTLE = new MortarAndPestle();
	// Essences will mostly beecipeidentical in functionality (i.e. none) and simply exist as crafting ingredients
	public static final Item HEALING_ESSENCE = new Essence();
	public static final Item PURITY_ESSENCE = new Essence();

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
		new Identifier(MODID, "general"))
		.icon(() -> new ItemStack(STONE_CRUCIBLE))
		.appendItems(stacks -> {
			stacks.add(new ItemStack(STONE_CRUCIBLE));
			stacks.add(new ItemStack(VIAL));
			stacks.add(new ItemStack(FILLED_VIAL));
			stacks.add(new ItemStack(MORTAR_AND_PESTLE));
			stacks.add(new ItemStack(HEALING_ESSENCE));
			stacks.add(new ItemStack(PURITY_ESSENCE));
		})
		.build();
	
	
	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AlchemyRecipeManager());
		
		Registry.register(Registry.BLOCK, new Identifier(MODID, "stone_crucible"), STONE_CRUCIBLE);
		// Builder<CrucibleEntity> builder = ;
		crucibleBlockEntity = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			MODID + ":stone_crucible",
			FabricBlockEntityTypeBuilder.create(CrucibleEntity::new, STONE_CRUCIBLE).build(null)
		);
		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "stone_crucible"),
			new BlockItem(STONE_CRUCIBLE, new Item.Settings().group(AlchemicalBrewing.ITEM_GROUP))
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

		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "mortar_and_pestle"),
			MORTAR_AND_PESTLE
		);

		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "healing_essence"),
			HEALING_ESSENCE
		);

		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "purity_essence"),
			PURITY_ESSENCE
		);

		AlchemyEffectRegistry.register(new Saturation(new Identifier(MODID, "stew")));
		AlchemyEffectRegistry.register(new Healing(new Identifier(MODID, "healing")));
		//TODO: Rename the effect or fluid
		AlchemyFluidRegistry.register(new setadokalo.alchemicalbrewing.fluids.Healing(new Identifier(MODID, "healing")));
		AlchemyFluidRegistry.register(new setadokalo.alchemicalbrewing.fluids.Stew(new Identifier(MODID, "stew")));
	}
	public static void log(Level level, String message){
		LOGGER.log(level, LOG_STRING, message);
	}
}
