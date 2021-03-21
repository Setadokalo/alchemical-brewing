package setadokalo.alchemicalbrewing.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class Essence extends Item {

	public Essence() {
		super(new Properties().tab(AlchemicalBrewing.ITEM_GROUP).rarity(Rarity.UNCOMMON));
	}
	
}
