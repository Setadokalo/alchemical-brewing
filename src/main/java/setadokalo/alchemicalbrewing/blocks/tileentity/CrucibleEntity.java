package setadokalo.alchemicalbrewing.blocks.tileentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.logging.log4j.Level;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
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
	private ArrayList<ItemStack> allItemsInPot = new ArrayList<>();
	private ArrayList<CookingItemStack> itemsInPot = new ArrayList<>();
	private ArrayList<ItemStack> readyItemsInPot = new ArrayList<>();
	private ArrayList<ConcentratedFluid> fluidsInPot = new ArrayList<>();

	void add(ItemStack stack) {
		add(new CookingItemStack(stack, DEFAULT_COOK_TIME));
	}
	void add(ItemStack stack, int cookTime) {
		add(new CookingItemStack(stack, cookTime));
	}

	void add(CookingItemStack iStack) {
		allItemsInPot.add(iStack.itemStack);
		itemsInPot.add(iStack);
	}

	public List<ItemStack> getItemsInPot() {
		return allItemsInPot;
	}

	public CrucibleEntity(BlockPos pos, BlockState state, int maxWater, int maxIngredients) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
		maxWaterCapacity = maxWater;
		maxIngredientCapacity = maxIngredients;
	}
	public CrucibleEntity(BlockPos pos, BlockState state) {
		super(AlchemicalBrewing.crucibleBlockEntity, pos, state);
		maxWaterCapacity = 9;
		maxIngredientCapacity = 32;
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
		int ret = removeLevelsNoEmpty(amount, takeLess, ignorePurity);
		if (this.level == 0)
			this.emptyPot();
		return ret;
	}
	private int removeLevelsNoEmpty(int amount, boolean takeLess, boolean ignorePurity) {
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

	@Override
	public void markDirty() {
		super.markDirty();
		sync();
	}

	public CompoundTag takeLevels(int amount) {
		int oldLevel = this.level;
		if (removeLevelsNoEmpty(amount, false, true) == 0)
			return null;
		BigFraction fracToTake = new BigFraction(amount, oldLevel);
		ConcentratedFluid[] effectArray = new ConcentratedFluid[fluidsInPot.size()];
		for (int i = 0; i < fluidsInPot.size(); i++) {
			ConcentratedFluid effect = fluidsInPot.get(i).split(fracToTake);
			effectArray[i] = effect;
		}
		CompoundTag tag = new CompoundTag();
		tag.put("Effects", FilledVial.getTagForFluids(effectArray));
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
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level).with(Crucible.READY, this.isReady()), 2);
		markDirty();
		return amount;
	}

	public boolean addItem(ItemStack stack) {
		stack = stack.copy();
		if (this.isReady() && this.itemsInPot.size() < this.maxIngredientCapacity) {
			if (stack.getCount() > 1) {
				int count = stack.getCount();
				stack.setCount(1);
				for (; count > 0; count--) {
					this.add(stack.copy());
				}
			} else {
				this.add(stack);
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
		this.fluidsInPot.clear();
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
		if (uEntity.getWorld() != null && !uEntity.getWorld().isClient) {
			if (!(uEntity instanceof CrucibleEntity)) {
				return;
			}
			CrucibleEntity entity = (CrucibleEntity) uEntity;
			if (entity.tickToReady()) {
				entity.tickItems();
			}
		}
	}

	protected void tickItems() {
		// avoiding a concurrentmodificationexception
		ArrayList<CookingItemStack> itemsToRemove = new ArrayList<>();
		for (CookingItemStack cookingItem: this.itemsInPot) {
			if (cookingItem.tickCooking()) {
				itemsToRemove.add(cookingItem);
				this.readyItemsInPot.add(cookingItem.itemStack);
				// we only need to try recipes when a new item enters the ingredient pool
				// (since without a new item any valid recipe should have already been completed)
				this.tryAllRecipes();
			}
		}
		for (CookingItemStack item : itemsToRemove) {
			this.itemsInPot.remove(item);
		}
		
	}

	//TODO: There's probably a more efficient way to do this
	protected void tryAllRecipes() {
		boolean didRecipe = false;
		// for each recipe, try performing the recipe (as many times as possible with the ingredients in the pot)
		for (AlchemyRecipe recipe: AlchemyRecipeRegistry.values()) {
			while (tryPerformRecipe(recipe)) {
				didRecipe = true;
				AlchemicalBrewing.log(Level.INFO, "performed recipe " + recipe);
				MutableText textToSend = new TranslatableText("message.alchemicalbrewing.recipefinished");
				String[] translationKeyFragments = recipe.getIdentifier().toString().split(":");
				String transKey = translationKeyFragments[0] + "." + translationKeyFragments[1];
				textToSend.append(new TranslatableText("recipe." + transKey));
				for (PlayerEntity player : this.getWorld().getPlayers()) {
					
					player.sendMessage(textToSend, false);
				}
			}
		}
		if (didRecipe) {
			this.world.setBlockState(this.pos, this.world.getBlockState(this.pos), 2);
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
			this.allItemsInPot.remove(item);
		}

		for (ConcentratedFluid result : recipe.results) {
			this.addFluidToPot(result);
		}

		return true;
	}

	public void addFluidToPot(ConcentratedFluid fluidToAdd) {
		// BigFraction totalConcentration = BigFraction.ZERO;
		// for (ConcentratedFluid fluidInPot: this.fluidsInPot) {
		// 	totalConcentration = totalConcentration.add(fluidInPot.concentration);
		// }
		// if ((totalConcentration.add(fluidToAdd.concentration).compareTo(new BigFraction(10 * this.level, 1))) > 0) {
		// 	fluidToAdd.concentration = new BigFraction(10 * this.level, 1).subtract(totalConcentration);
		// 	if (fluidToAdd.concentration.compareTo(BigFraction.ZERO) <= 0)
		// 		return;
		// }
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
		tag = super.toTag(tag);

		// Save the current value of the number to the tag
		tag.putInt("TicksToReady", this.ticksToReady);
		tag.putInt("Level", this.level);
		
		ListTag allTag = new ListTag();

		for(int i = 0; i < this.allItemsInPot.size(); ++i) {
			ItemStack item = this.allItemsInPot.get(i);
			CompoundTag compoundTag = item.toTag(new CompoundTag());
			allTag.add(compoundTag);
		}

		if (!allTag.isEmpty()) {
			tag.put("AllItems", allTag);
		}

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
		this.ticksToReady = tag.getInt("TicksToReady");
		this.level = tag.getInt("Level");
		
		ListTag listTag = tag.getList("Items", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.itemsInPot.add(CookingItemStack.fromTag(compoundTag));
		}
		
		listTag = tag.getList("AllItems", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.allItemsInPot.add(ItemStack.fromTag(compoundTag));
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
		this.fluidsInPot.clear();
		this.allItemsInPot.clear();
		this.itemsInPot.clear();
		this.readyItemsInPot.clear();
		this.fromTag(tag);
		MinecraftClient instance = MinecraftClient.getInstance();
		instance.worldRenderer.scheduleBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return this.toTag(tag);
	}
}
