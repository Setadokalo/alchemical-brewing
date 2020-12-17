package setadokalo.alchemicalbrewing.item;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluidEffect;
import setadokalo.alchemicalbrewing.util.Color;

public class FilledVial extends Item {

	public FilledVial() {
		super(new Settings().group(ItemGroup.BREWING).maxCount(64));
	}

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.DRINK;
	}
	
   public int getMaxUseTime(ItemStack stack) {
      return 32;
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		List<ConcentratedFluidEffect> effects = getEffects(stack);
		for (ConcentratedFluidEffect effect : effects) {
			tooltip.add(effect.getTooltip());
		}
	}
	
	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		List<ConcentratedFluidEffect> effects = getEffects(stack);
		if (!world.isClient) {
			if (user instanceof PlayerEntity) {
				if (!((PlayerEntity)user).abilities.creativeMode) {
					stack.decrement(1);
					((PlayerEntity) user).giveItemStack(new ItemStack(AlchemicalBrewing.VIAL, 1));
				}
			}
		}

		for (ConcentratedFluidEffect cEffect : effects) {
			cEffect.effect.applyEffect(world, user, cEffect.concentration);
		}
		return stack;
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

	public static ListTag getTagForEffects(ConcentratedFluidEffect... effects) {
		ListTag effectList = new ListTag();
		for (ConcentratedFluidEffect effect : effects) {
			CompoundTag tag = new CompoundTag();
			effect.toTag(tag);
			effectList.add(tag);
		}
		return effectList;
	}


	public static int getColorForStack(ItemStack stack) {
		if (stack.getItem() == AlchemicalBrewing.FILLED_VIAL) {
			List<ConcentratedFluidEffect> effects = FilledVial.getEffects(stack);
			// colors.add(Color.WATER);
			Fraction totalConcentration = Fraction.ZERO;
			for (ConcentratedFluidEffect effect : effects) {
				totalConcentration = totalConcentration.add(effect.concentration);
			}
			Color totalColor = Color.BLACK;
			double dTotalCon = totalConcentration.doubleValue();
			for (ConcentratedFluidEffect effect : effects) {
				Color currentColor = effect.effect.getColor(stack);
				currentColor = currentColor.mul(effect.concentration.doubleValue() / dTotalCon);
				totalColor = totalColor.add(currentColor);
			}
			if (dTotalCon < 1.0) {
				totalColor = Color.WATER.mix(totalColor, dTotalCon);
			}
			return totalColor.asInt();
		}
		return 0;
	}
}
