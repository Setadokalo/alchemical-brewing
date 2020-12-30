package setadokalo.alchemicalbrewing.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.RaycastContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import net.minecraft.util.hit.BlockHitResult;

public class Vial extends Item {

	public Vial() {
		super(new Settings().group(ItemGroup.BREWING));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
		if (hitResult.getType() == HitResult.Type.MISS) {
			return TypedActionResult.pass(itemStack);
		} else {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
				if (!world.canPlayerModifyAt(user, blockPos)) {
					return TypedActionResult.pass(itemStack);
				}

				if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
					world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);

					if (!world.isClient && user instanceof PlayerEntity && !user.getAbilities().creativeMode) {
						itemStack.decrement(1);
						user.giveItemStack(new ItemStack(AlchemicalBrewing.FILLED_VIAL, 1));
					}
					return TypedActionResult.success(itemStack, world.isClient());
				}
			}

			return TypedActionResult.pass(itemStack);
		}
   }
}
