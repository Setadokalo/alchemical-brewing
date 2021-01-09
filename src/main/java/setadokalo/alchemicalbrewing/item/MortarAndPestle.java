package setadokalo.alchemicalbrewing.item;

import net.minecraft.item.Item;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class MortarAndPestle extends Item {

	public MortarAndPestle() {
		super(new Settings().group(AlchemicalBrewing.ITEM_GROUP).maxCount(1));
	}
	
}
