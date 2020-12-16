package setadokalo.alchemicalbrewing.item;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluidEffect;

//TODO extract Effects from filled crucibles
public class FilledVial extends Item {

	public FilledVial() {
		super(new Settings());
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		List<ConcentratedFluidEffect> effects = getEffects(itemStack);
		for (ConcentratedFluidEffect cEffect : effects) {
			cEffect.effect.applyEffect(world, user, cEffect.concentration);
		}

		return TypedActionResult.pass(itemStack);
   }

	public static List<ConcentratedFluidEffect> getEffects(ItemStack stack) {
		return getEffectsForTag(stack.getTag());
	}

   public static List<ConcentratedFluidEffect> getEffectsForTag(@Nullable CompoundTag tag) {
      List<ConcentratedFluidEffect> list = Lists.newArrayList();
      getEffectsForTag(tag, list);
      return list;
   }

	public static void getEffectsForTag(CompoundTag tag, List<ConcentratedFluidEffect> list) {
      if (tag != null && tag.contains("Effects", 9)) {
         ListTag listTag = tag.getList("Effects", 10);

         for(int i = 0; i < listTag.size(); ++i) {
				CompoundTag compoundTag = listTag.getCompound(i);
            ConcentratedFluidEffect effect = ConcentratedFluidEffect.fromTag(compoundTag);
            if (effect != null) {
               list.add(effect);
            }
         }
      }
	}

	public static int getColorForStack(ItemStack stack) {
		//TODO get color by polling each effect in the stack for it's color and mixing them together
		return 0;
	}
}
