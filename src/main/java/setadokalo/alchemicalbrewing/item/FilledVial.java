package setadokalo.alchemicalbrewing.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FilledVial extends Item {

	public FilledVial() {
		super(new Settings());
	}

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		
		return TypedActionResult.pass(itemStack);
   }

}
