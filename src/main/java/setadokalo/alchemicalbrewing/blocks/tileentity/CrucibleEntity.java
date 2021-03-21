package setadokalo.alchemicalbrewing.blocks.tileentity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.logging.log4j.Level;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

		public CompoundTag writeNbt(CompoundTag iTag) {
			iTag = itemStack.save(iTag);
			iTag.putInt(REMAINING_TAG, cookTimeRemaining);
			return iTag;
		}

		public static CookingItemStack fromTag(CompoundTag tag) {
			return new CookingItemStack(ItemStack.of(tag), tag.getInt(REMAINING_TAG));
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
	private int waterLevel = 0;
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
	public void setWaterLevel(int newLevel) {
		if (newLevel > waterLevel) {
			this.addLevels(newLevel - waterLevel, true);
		} else {
			this.removeLevels(waterLevel - newLevel, true, true);
		}
	}
	public int getWaterLevel() {
		return waterLevel;
	}

	public int removeLevels(int amount, boolean takeLess, boolean ignorePurity) {
		int ret = removeLevelsNoEmpty(amount, takeLess, ignorePurity);
		if (this.waterLevel == 0)
			this.emptyPot();
		return ret;
	}
	private int removeLevelsNoEmpty(int amount, boolean takeLess, boolean ignorePurity) {
		if (ignorePurity || this.isPureWater()) {
			if (amount > waterLevel) {
				if (takeLess) {
					amount = waterLevel;
				} else {
					return 0;
				}
			}
			waterLevel = waterLevel - amount;
			// the amount of ticks to warm up should be no more than the amount of ticks to warm up this much liquid from scratch
			ticksToReady = Math.min(ticksToReady, waterLevel * 20);
			this.getLevel().setBlock(this.worldPosition, this.getLevel().getBlockState(this.worldPosition).setValue(Crucible.LEVEL, waterLevel), 2);
			setChanged();
			return amount;
		}
		return 0;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		sync();
	}

	public CompoundTag takeLevels(int amount) {
		int oldLevel = this.waterLevel;
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
		if (amount + waterLevel > this.maxWaterCapacity) {
			if (addLess) {
				amount = this.maxWaterCapacity - waterLevel;
			} else {
				return 0;
			}
		}
		ticksToReady += 20 * amount;
		waterLevel = waterLevel + amount;
		assert this.level != null;
		this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(Crucible.LEVEL, waterLevel).setValue(Crucible.READY, this.isReady()), 2);
		setChanged();
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
			AlchemicalBrewing.log(Level.INFO, "Added item " + Registry.ITEM.getKey(stack.getItem()).toString() + " to pot");
			setChanged();
			return true;
		}
		return false;
	}

	public void emptyPot() {
		this.itemsInPot.clear();
		this.readyItemsInPot.clear();
		this.fluidsInPot.clear();
		this.waterLevel = 0;
		this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(Crucible.LEVEL, waterLevel), 2);
	}

	public boolean isReady() {
		return waterLevel > 0 && ticksToReady == 0;
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
				if (this.level != null && !this.level.isClientSide()) {
					this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(Crucible.READY, this.isReady()), 2);
				}
			}
			setChanged();
		}
		return this.isReady();
	}


	public static void tick(BlockEntity uEntity) {
		if (uEntity.getLevel() != null && !uEntity.getLevel().isClientSide) {
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
				MutableComponent textToSend = new TranslatableComponent("message.alchemicalbrewing.recipefinished");
				String[] translationKeyFragments = recipe.getIdentifier().toString().split(":");
				String transKey = translationKeyFragments[0] + "." + translationKeyFragments[1];
				textToSend.append(new TranslatableComponent("recipe." + transKey));
				for (Player player : this.getLevel().players()) {
					
					player.displayClientMessage(textToSend, false);
				}
			}
		}
		if (didRecipe) {
			this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition), 2);
			setChanged();
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
			if (requiredItem.matches(item)) {
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

	@Override
	// Serialize the BlockEntity
	public CompoundTag save(CompoundTag tag) {
		tag = super.save(tag);

		// Save the current value of the number to the tag
		tag.putInt("TicksToReady", this.ticksToReady);
		tag.putInt("Level", this.waterLevel);
		
		ListTag allTag = new ListTag();

		for(int i = 0; i < this.allItemsInPot.size(); ++i) {
			ItemStack item = this.allItemsInPot.get(i);
			CompoundTag NbtCompound = item.save(new CompoundTag());
			allTag.add(NbtCompound);
		}

		if (!allTag.isEmpty()) {
			tag.put("AllItems", allTag);
		}

		ListTag NbtList = new ListTag();

		for(int i = 0; i < this.itemsInPot.size(); ++i) {
			CookingItemStack cookingItem = this.itemsInPot.get(i);
			CompoundTag NbtCompound = cookingItem.writeNbt(new CompoundTag());
			NbtList.add(NbtCompound);
		}

		if (!NbtList.isEmpty()) {
			tag.put("Items", NbtList);
		}
		ListTag readyTag = new ListTag();

		for(int i = 0; i < this.readyItemsInPot.size(); ++i) {
			ItemStack cookingItem = this.readyItemsInPot.get(i);
			CompoundTag NbtCompound = cookingItem.save(new CompoundTag());
			readyTag.add(NbtCompound);
		}

		if (!readyTag.isEmpty()) {
			tag.put("ReadyItems", readyTag);
		}
		ListTag fluidsTag = new ListTag();

		for(int i = 0; i < this.fluidsInPot.size(); ++i) {
			ConcentratedFluid fluid = this.fluidsInPot.get(i);
			CompoundTag NbtCompound = fluid.writeNbt(new CompoundTag());
			fluidsTag.add(NbtCompound);
		}

		if (!fluidsTag.isEmpty()) {
			tag.put("Fluids", fluidsTag);
		}

		return tag;
	}
	
	// Deserialize the BlockEntity
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.ticksToReady = tag.getInt("TicksToReady");
		this.waterLevel = tag.getInt("Level");
		
		ListTag NbtList = tag.getList("Items", 10);

		for(int i = 0; i < NbtList.size(); ++i) {
			CompoundTag NbtCompound = NbtList.getCompound(i);
			this.itemsInPot.add(CookingItemStack.fromTag(NbtCompound));
		}
		
		NbtList = tag.getList("AllItems", 10);

		for(int i = 0; i < NbtList.size(); ++i) {
			CompoundTag NbtCompound = NbtList.getCompound(i);
			this.allItemsInPot.add(ItemStack.of(NbtCompound));
		}
		
		NbtList = tag.getList("ReadyItems", 10);

		for(int i = 0; i < NbtList.size(); ++i) {
			CompoundTag NbtCompound = NbtList.getCompound(i);
			this.readyItemsInPot.add(ItemStack.of(NbtCompound));
		}
		NbtList = tag.getList("Fluids", 10);

		for(int i = 0; i < NbtList.size(); ++i) {
			CompoundTag NbtCompound = NbtList.getCompound(i);
			this.fluidsInPot.add(ConcentratedFluid.fromTag(NbtCompound));
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
		this.load(tag);
		Minecraft instance = Minecraft.getInstance();
		instance.levelRenderer.setBlocksDirty(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return this.save(tag);
	}
}
