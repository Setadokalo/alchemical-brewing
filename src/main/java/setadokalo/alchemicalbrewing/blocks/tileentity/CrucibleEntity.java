package setadokalo.alchemicalbrewing.blocks.tileentity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.logging.log4j.Level;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.Crucible;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluidEffect;
import setadokalo.alchemicalbrewing.item.FilledVial;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;
import setadokalo.alchemicalbrewing.registry.AlchemyRecipeRegistry;

public class CrucibleEntity extends BlockEntity {
	private class CookingItemStack {
		ItemStack itemStack;
		int cookTimeRemaining;
		CookingItemStack(ItemStack stack, int cookTime) {
			itemStack = stack;
			cookTimeRemaining = cookTime;
		}
		boolean tickCooking() {
			if (cookTimeRemaining > 0)
				cookTimeRemaining -= 1;
			return cookTimeRemaining <= 0;
		}
	}

	/** The number of ticks until the crucible is ready to be used. */
	private int ticksToReady = 0;
	/** The maximum capacity of this crucible (allowing this one base class to be reused). */
	public final int maxWaterCapacity;
	public final int maxIngredientCapacity;
	
	// 4 seconds * 20 ticks per second = 160 ticks to cook
	public static final int DEFAULT_COOK_TIME = 80;
	/** The current number of bottles of water in this crucible. One bucket = 9 bottles, because how TF does
	  * one bottle contain a *third of a cubic meter* of water, Mojang? */
	private int level = 0;
	private ArrayList<CookingItemStack> itemsInPot = new ArrayList<>();
	private ArrayList<ItemStack> readyItemsInPot = new ArrayList<>();
	private ArrayList<ConcentratedFluidEffect> effectsInPot = new ArrayList<>();

	void add(ArrayList<CookingItemStack> iIP, ItemStack stack) {
		iIP.add(new CookingItemStack(stack, DEFAULT_COOK_TIME));
	}
	void add(ArrayList<CookingItemStack> iIP, ItemStack stack, int cookTime) {
		iIP.add(new CookingItemStack(stack, cookTime));
	}

	public CrucibleEntity(BlockPos pos, BlockState state, int maxWater, int maxIngredients) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		maxWaterCapacity = maxWater;
		maxIngredientCapacity = maxIngredients;
	}
	public CrucibleEntity(BlockPos pos, BlockState state) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		maxWaterCapacity = 9;
		maxIngredientCapacity = 16;
	}
	public void setLevel(int newLevel) {
		if (newLevel > level) {
			this.addLevels(newLevel - level, true);
		} else {
			this.removeLevels(level - newLevel, true, true);
		}
	}
	public int getLevel() {
		return level;
	}

	public int removeLevels(int amount, boolean takeLess, boolean ignorePurity) {
		if (ignorePurity || this.isPureWater()) {
			if (amount > level) {
				if (takeLess) {
					amount = level;
				} else {
					return 0;
				}
			}
			level = level - amount;
			// the amount of ticks to warm up should be no more than the amount of ticks to warm up this much liquid from scratch
			ticksToReady = Math.min(ticksToReady, level * 20);
			this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
			markDirty();
			return amount;
		}
		return 0;
	}

	public CompoundTag takeLevels(int amount) {
		if (level < amount)
			return null;
		Fraction fracToTake = new Fraction(amount, level);
		ConcentratedFluidEffect[] effectArray = new ConcentratedFluidEffect[effectsInPot.size()];
		for (int i = 0; i < effectsInPot.size(); i++) {
			ConcentratedFluidEffect effect = effectsInPot.get(i).split(fracToTake);
			effectArray[i] = effect;
		}
		CompoundTag tag = new CompoundTag();
		tag.put("Effects", FilledVial.getTagForEffects(effectArray));
		this.level -= amount;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
		return tag;
	}


	public int addLevels(int amount, boolean addLess) {
		if (amount + level > this.maxWaterCapacity) {
			if (addLess) {
				amount = this.maxWaterCapacity - level;
			} else {
				return 0;
			}
		}
		ticksToReady += 20 * amount;
		level = level + amount;
		assert this.world != null;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
		markDirty();
		return amount;
	}

	// `stack` should be treated as consumed by this method; in rust terms, ownership is transferred here to this class.
	public boolean addItem(ItemStack stack) {
		if (this.isReady() && this.itemsInPot.size() < this.maxIngredientCapacity) {
			this.add(itemsInPot, stack);
			AlchemicalBrewing.log(Level.INFO, "Added item " + stack.getItem().toString() + " to pot");
			markDirty();
			return true;
		}
		return false;
	}

	public void emptyPot() {
		this.itemsInPot.clear();
		this.readyItemsInPot.clear();
		this.level = 0;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
	}

	public boolean isReady() {
		return level > 0 && ticksToReady == 0;
	}

	public boolean isPureWater() {
		return this.itemsInPot.isEmpty() && this.readyItemsInPot.isEmpty();
	}

	public boolean tickToReady() {
		
		if (!this.isReady()) {
			ticksToReady -= 1;
			if (ticksToReady == 0) {
				AlchemicalBrewing.log(Level.INFO, "Crucible is ready");
				if (this.world != null && !this.world.isClient()) {
					this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.READY, this.isReady()), 2);
				}
			}
			markDirty();
		}
		return this.isReady();
	}


	public static void tick(BlockEntity uEntity) {
		if (!(uEntity instanceof CrucibleEntity)) {
			return;
		}
		CrucibleEntity entity = (CrucibleEntity) uEntity;
		if (entity.tickToReady()) {
			ArrayList<CookingItemStack> itemsToRemove = new ArrayList<>();
			for (CookingItemStack cookingItem: entity.itemsInPot) {
				if (cookingItem.tickCooking()) {
					itemsToRemove.add(cookingItem);
					entity.readyItemsInPot.add(cookingItem.itemStack);
					entity.tryAllRecipes();
				}
			}
			for (CookingItemStack item : itemsToRemove) {
				entity.itemsInPot.remove(item);
			}
		}
	}

	protected void tryAllRecipes() {
		// for each recipe, try performing the recipe (as many times as possible with the ingredients in the pot)
		for (AlchemyRecipe recipe: AlchemyRecipeRegistry.values()) {
			while (tryPerformRecipe(recipe)) {
				AlchemicalBrewing.log(Level.INFO, "performed recipe " + recipe);

				MutableText textToSend = new TranslatableText("message.alchemicalbrewing.recipefinished");
				String[] translationKeyFragments = recipe.getIdentifier().toString().split(":");
				String transKey = translationKeyFragments[0] + "." + translationKeyFragments[1];
				textToSend.append(new TranslatableText("fluideffect.name." + transKey));
				for (PlayerEntity player : this.getWorld().getPlayers()) {
					
					player.sendMessage(textToSend, false);
				}
			}
		}
	}

	protected boolean tryPerformRecipe(AlchemyRecipe recipe) {
		ArrayList<ItemStack> list = (ArrayList<ItemStack>) this.readyItemsInPot.clone();
		ArrayList<ItemStack> foundItems = new ArrayList<>();
		for (Item requiredItem: recipe.ingredients) {
			if (!isItemInPot(list, requiredItem, foundItems))
				return false;
		}
		for (ItemStack item: foundItems) {
			this.readyItemsInPot.remove(item);
		}

		for (ConcentratedFluidEffect result : recipe.results) {
			this.addEffectToPot(result);
		}

		return true;
	}

	public void addEffectToPot(ConcentratedFluidEffect effectToAdd) {
		boolean resultFoundInPot = false;
		for (ConcentratedFluidEffect effectInPot : this.effectsInPot) {
			if (effectInPot.effect == effectToAdd.effect) {
				effectInPot.concentration = effectInPot.concentration.add(effectToAdd.concentration);
				resultFoundInPot = true;
				break;
			}
		}
		if (!resultFoundInPot)
			this.effectsInPot.add(effectToAdd.clone());
	}
	protected boolean isItemInPot(List<ItemStack> list, Item requiredItem, List<ItemStack> moveToList) {
		ItemStack foundItem = null;
		for (ItemStack item: list) {
			if (item.getItem() == requiredItem) {
				foundItem = item;
				break;
			}
		}
		if (foundItem != null) {
			list.remove(foundItem);
			moveToList.add(foundItem);
			return true;
		}
		return false;
	}


	// Serialize the BlockEntity
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);

		// Save the current value of the number to the tag
		tag.putInt("ticksToReady", this.ticksToReady);
		tag.putInt("level", this.level);
		
		ListTag listTag = new ListTag();

		for(int i = 0; i < this.itemsInPot.size(); ++i) {
			CookingItemStack cookingItem = this.itemsInPot.get(i);
			CompoundTag compoundTag = new CompoundTag();
			cookingItem.itemStack.toTag(compoundTag);
			compoundTag.putInt("cookTimeRemaining", cookingItem.cookTimeRemaining);
			listTag.add(compoundTag);
		}

		if (!listTag.isEmpty()) {
			tag.put("Items", listTag);
		}
		ListTag readyTag = new ListTag();

		for(int i = 0; i < this.itemsInPot.size(); ++i) {
			ItemStack cookingItem = this.readyItemsInPot.get(i);
			CompoundTag compoundTag = new CompoundTag();
			cookingItem.toTag(compoundTag);
			readyTag.add(compoundTag);
		}

		if (!readyTag.isEmpty()) {
			tag.put("ReadyItems", readyTag);
		}

		return tag;
	}
	
	// Deserialize the BlockEntity
	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		this.ticksToReady = tag.getInt("ticksToReady");
		this.level = tag.getInt("level");
		
		ListTag listTag = tag.getList("Items", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.add(this.itemsInPot, ItemStack.fromTag(compoundTag), compoundTag.getInt("cookTimeRemaining"));
		}
		listTag = tag.getList("ReadyItems", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.readyItemsInPot.add(ItemStack.fromTag(compoundTag));
		}
	}
	public List<ConcentratedFluidEffect> getEffects() {
		return (List<ConcentratedFluidEffect>) this.effectsInPot.clone();
	}
}
