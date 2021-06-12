package setadokalo.alchemicalbrewing.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class Vial extends Item {

	public Vial() {
		super(new Properties().tab(AlchemicalBrewing.ITEM_GROUP));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
		HitResult hitResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
		if (hitResult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
				if (!world.mayInteract(player, blockPos)) {
					return InteractionResultHolder.pass(itemStack);
				}

				if (world.getFluidState(blockPos).is(FluidTags.WATER)) {
					world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);

					if (!world.isClientSide && !player.getAbilities().instabuild) {
						itemStack.shrink(1);
						player.addItem(new ItemStack(ABItems.FILLED_VIAL, 1));
					}
					return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
				}
			}

			return InteractionResultHolder.pass(itemStack);
		}
   }
}
