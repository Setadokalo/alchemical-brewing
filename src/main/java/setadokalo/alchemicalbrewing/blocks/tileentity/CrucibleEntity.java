package setadokalo.alchemicalbrewing.blocks.tileentity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.logging.log4j.Level;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.Crucible;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.item.FilledVial;
import setadokalo.alchemicalbrewing.recipe.AlchemyRecipe;
import setadokalo.alchemicalbrewing.registry.AlchemyRecipeRegistry;

//TODO: 
public class CrucibleEntity extends BlockEntity implements BlockEntityClientSerializable {
	private static class CookingItemStack {
		ItemStack itemStack;
		int cookTimeRemaining;
		public static final transient String REMAINING_TAG = "cookTimeRemaining";

		CookingItemStack(ItemStack stack, int cookTime) {
			itemStack = stack;
			cookTimeRemaining = cookTime;
		}

		boolean tickCooking() {
			if (cookTimeRemaining > 0)
				cookTimeRemaining -= 1;
			return cookTimeRemaining <= 0;
		}

		public CompoundTag toTag(CompoundTag iTag) {
			iTag = itemStack.toTag(iTag);
			iTag.putInt(REMAINING_TAG, cookTimeRemaining);
			return iTag;
		}

		public static CookingItemStack fromTag(CompoundTag compoundTag) {
			return new CookingItemStack(ItemStack.fromTag(compoundTag), compoundTag.getInt(REMAINING_TAG));
		}
	}

	/** The number of ticks until the crucible is ready to be used. */
	private int ticksToReady = 0;
	/** The maximum capacity of this crucible (allowing this one base class to be reused). */
	public final int maxWaterCapacity;
	public final int maxIngredientCapacity;
	
	// 4 seconds * 20 ticks per second = 80 ticks to cook
	public static final int DEFAULT_COOK_TIME = 80;
	/** The current number of bottles of water in this crucible. One bucket = 9 bottles, because how TF does
	  * one bottle contain a *third of a cubic meter* of water, Mojang? */
	private int level = 0;
	private ArrayList<CookingItemStack> itemsInPot = new ArrayList<>();
	private ArrayList<ItemStack> readyItemsInPot = new ArrayList<>();
	private ArrayList<ConcentratedFluid> fluidsInPot = new ArrayList<>();

	void add(ArrayList<CookingItemStack> iIP, ItemStack stack) {
		iIP.add(new CookingItemStack(stack, DEFAULT_COOK_TIME));
	}
	void add(ArrayList<CookingItemStack> iIP, ItemStack stack, int cookTime) {
		iIP.add(new CookingItemStack(stack, cookTime));
	}
	void add(ArrayList<CookingItemStack> iIP, CookingItemStack iStack) {
		iIP.add(iStack);
	}

	public CrucibleEntity(BlockPos pos, BlockState state, int maxWater, int maxIngredients) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
		maxWaterCapacity = maxWater;
		maxIngredientCapacity = maxIngredients;
	}
	public CrucibleEntity(BlockPos pos, BlockState state) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
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
		ConcentratedFluid[] effectArray = new ConcentratedFluid[fluidsInPot.size()];
		for (int i = 0; i < fluidsInPot.size(); i++) {
			ConcentratedFluid effect = fluidsInPot.get(i).split(fracToTake);
			effectArray[i] = effect;
		}
		CompoundTag tag = new CompoundTag();
		tag.put("Effects", FilledVial.getTagForFluids(effectArray));
		this.level -= amount;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
		return tag;
	}


	public int addLevels(int amount, boolean addLess) {
		AlchemicalBrewing.log(Level.INFO, "Adding levels to pot");
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
			if (stack.getCount() > 1) {
				stack = stack.copy();
				int count = stack.getCount();
				stack.setCount(1);
				for (; count > 0; count--) {
					this.add(itemsInPot, stack.copy());
				}
			} else {
				this.add(itemsInPot, stack);
			}
			AlchemicalBrewing.log(Level.INFO, "Added item " + Registry.ITEM.getId(stack.getItem()).toString() + " to pot");
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
		//TODO: maintaining three lists in the pot gets gross. Look into simplifying it to 1 or 2
		return this.itemsInPot.isEmpty() && this.readyItemsInPot.isEmpty() && this.fluidsInPot.isEmpty();
	}

	public boolean tickToReady() {
		
		if (!this.isReady()) {
			ticksToReady -= 1;
			if (ticksToReady <= 0) {
				ticksToReady = 0;
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
		boolean didRecipe = false;
		// for each recipe, try performing the recipe (as many times as possible with the ingredients in the pot)
		for (AlchemyRecipe recipe: AlchemyRecipeRegistry.values()) {
			while (tryPerformRecipe(recipe)) {
				didRecipe = true;
				AlchemicalBrewing.log(Level.INFO, "performed recipe " + recipe);
				//TODO: maybe make this use a translation key from the recipe itself? or just remove the
				//TODO: translatable key for it entirely
				MutableText textToSend = new TranslatableText("message.alchemicalbrewing.recipefinished");
				String[] translationKeyFragments = recipe.getIdentifier().toString().split(":");
				String transKey = translationKeyFragments[0] + "." + translationKeyFragments[1];
				textToSend.append(new TranslatableText("name.fluid." + transKey));
				for (PlayerEntity player : this.getWorld().getPlayers()) {
					
					player.sendMessage(textToSend, false);
				}
			}
		}
		if (didRecipe) {
			markDirty();
		}
	}

	protected boolean tryPerformRecipe(AlchemyRecipe recipe) {
		List<ItemStack> list = (List<ItemStack>) this.readyItemsInPot.clone();
		List<ItemStack> foundItems = new ArrayList<>();
		for (ItemPredicate requiredItem: recipe.ingredients) {
			if (!isPredicateInPot(list, requiredItem, foundItems))
				return false;
		}
		for (ItemStack item: foundItems) {
			this.readyItemsInPot.remove(item);
		}

		for (ConcentratedFluid result : recipe.results) {
			this.addFluidToPot(result);
		}

		return true;
	}

	public void addFluidToPot(ConcentratedFluid fluidToAdd) {
		boolean resultFoundInPot = false;
		for (ConcentratedFluid fluidInPot : this.fluidsInPot) {
			if (fluidInPot.fluid == fluidToAdd.fluid) {
				fluidInPot.concentration = fluidInPot.concentration.add(fluidToAdd.concentration);
				resultFoundInPot = true;
				break;
			}
		}
		if (!resultFoundInPot)
			this.fluidsInPot.add(fluidToAdd.clone());
	}
	protected boolean isPredicateInPot(List<ItemStack> list, ItemPredicate requiredItem, List<ItemStack> moveToList) {
		ItemStack foundItem = null;
		for (ItemStack item: list) {
			if (requiredItem.test(item)) {
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
			CompoundTag compoundTag = cookingItem.toTag(new CompoundTag());
			listTag.add(compoundTag);
		}

		if (!listTag.isEmpty()) {
			tag.put("Items", listTag);
		}
		ListTag readyTag = new ListTag();

		for(int i = 0; i < this.readyItemsInPot.size(); ++i) {
			ItemStack cookingItem = this.readyItemsInPot.get(i);
			CompoundTag compoundTag = cookingItem.toTag(new CompoundTag());
			readyTag.add(compoundTag);
		}

		if (!readyTag.isEmpty()) {
			tag.put("ReadyItems", readyTag);
		}
		ListTag fluidsTag = new ListTag();

		for(int i = 0; i < this.fluidsInPot.size(); ++i) {
			ConcentratedFluid fluid = this.fluidsInPot.get(i);
			CompoundTag compoundTag = fluid.toTag(new CompoundTag());
			fluidsTag.add(compoundTag);
		}

		if (!fluidsTag.isEmpty()) {
			tag.put("Fluids", fluidsTag);
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
			this.add(this.itemsInPot, CookingItemStack.fromTag(compoundTag));
		}
		listTag = tag.getList("ReadyItems", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.readyItemsInPot.add(ItemStack.fromTag(compoundTag));
		}
		listTag = tag.getList("Fluids", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.fluidsInPot.add(ConcentratedFluid.fromTag(compoundTag));
		}
	}

	public List<ConcentratedFluid> getEffects() {
		return (List<ConcentratedFluid>) this.fluidsInPot.clone();
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		this.fromTag(tag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return this.toTag(tag);
	}
}
