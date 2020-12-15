package setadokalo.alchemicalbrewing.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.reciperegistry.AlchemyRecipeRegistry;

public class AlchemyRecipe {
	public static final AlchemyRecipe EMPTY = register(
		new AlchemyRecipe(
			new Identifier(AlchemicalBrewing.MODID, "empty"), 
			Items.AIR, 
			-1, 
			Integer.MAX_VALUE
		)
	);

	private static AlchemyRecipe register(AlchemyRecipe alchemyRecipe) {
		return AlchemyRecipeRegistry.register(alchemyRecipe);
	}

	private Identifier identifier;

	public AlchemyRecipe(Identifier identifier, Item air, int i, int maxValue) {
	}

	public Identifier getIdentifier() {
		return identifier;
	}
}
