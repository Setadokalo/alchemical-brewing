package setadokalo.alchemicalbrewing.item;

import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class Essence extends Item {

	public Essence() {
		super(new Settings().group(AlchemicalBrewing.ITEM_GROUP).rarity(Rarity.UNCOMMON));
	}
	
}
