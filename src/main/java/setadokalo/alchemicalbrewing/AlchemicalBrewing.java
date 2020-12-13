package setadokalo.alchemicalbrewing;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import setadokalo.alchemicalbrewing.blocks.Crucible;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;

public class AlchemicalBrewing implements ModInitializer {

	public static Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "alchemicalbrewing";
	public static final String MOD_NAME = "Alchemical Brewing";

	public static final Block STONE_CRUCIBLE = new Crucible();
	public static BlockEntityType<CrucibleEntity> CRUCIBLE_BLOCK_ENTITY;

	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(
		new Identifier(MODID, "general"))
		.icon(() -> new ItemStack(STONE_CRUCIBLE))
		.appendItems(stacks -> {
			stacks.add(new ItemStack(STONE_CRUCIBLE));
		}).build();
	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "stone_crucible"), STONE_CRUCIBLE);
		CRUCIBLE_BLOCK_ENTITY = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			MODID + ":stone_crucible",
			BlockEntityType.Builder.create(CrucibleEntity::new, STONE_CRUCIBLE).build(null)
		);
		Registry.register(
			Registry.ITEM,
			new Identifier(MODID, "stone_crucible"),
			new BlockItem(STONE_CRUCIBLE, new Item.Settings().group(ItemGroup.BREWING))
		);
	}
	public static void log(Level level, String message){
		LOGGER.log(level, "["+MOD_NAME+"] " + message);
	}
}
