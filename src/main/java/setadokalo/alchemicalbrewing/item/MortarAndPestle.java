package setadokalo.alchemicalbrewing.item;

import java.lang.reflect.Field;

import net.minecraft.item.Item;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

//TODO: Give this a better recipe
public class MortarAndPestle extends Item {

	public MortarAndPestle() {
		super(new Settings().group(AlchemicalBrewing.ITEM_GROUP).maxCount(1));
		// the Item class uses a private final field to represent the item to be returned on using this item in a recipe
		// which is a problem for us, since we want the Mortar and Pestle to return *itself* when used
		// so to work around this we use reflection to set the returned item to be `this` after constructing `this`.
		//TODO there might be a better way to deal with this than using reflection
		Class<Item> itemC = Item.class;
		try {
			Field recipeRemainder = itemC.getDeclaredField("recipeRemainder"); //TODO change to "field_8008" on release
			// unless a better method is found, of course - that's why I'm not spending effort on making Gradle change it for me
			recipeRemainder.setAccessible(true);
			recipeRemainder.set(this, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
