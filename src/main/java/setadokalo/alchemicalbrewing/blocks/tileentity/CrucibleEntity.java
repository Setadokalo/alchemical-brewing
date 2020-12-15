package setadokalo.alchemicalbrewing.blocks.tileentity;

import java.util.LinkedList;

import org.apache.logging.log4j.Level;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Tickable;

import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.Crucible;

public class CrucibleEntity extends BlockEntity implements Tickable {
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
	private boolean wasReady = true;
	private int ticksToReady = 0;
	/** The maximum capacity of this crucible (allowing this one base class to be reused). */
	public final int MAX_WATER_CAPACITY;
	public final int MAX_INGREDIENT_CAPACITY;
	
	// 8 seconds * 20 ticks per second = 160 ticks to cook
	public static final int DEFAULT_COOK_TIME = 160;
	/** The current number of bottles of water in this crucible. One bucket = 9 bottles, because how TF does
	  * one bottle contain a *third of a cubic meter* of water, Mojang? */
	private int level = 0;
	private LinkedList<CookingItemStack> itemsInPot = new LinkedList<>();

	void add(LinkedList<CookingItemStack> iIP, ItemStack stack) {
		iIP.add(new CookingItemStack(stack, DEFAULT_COOK_TIME));
	}
	void add(LinkedList<CookingItemStack> iIP, ItemStack stack, int cookTime) {
		iIP.add(new CookingItemStack(stack, cookTime));
	}

	public CrucibleEntity(int max_water, int max_ingredients) {
		super(AlchemicalBrewing.CRUCIBLE_BLOCK_ENTITY);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		MAX_WATER_CAPACITY = max_water;
		MAX_INGREDIENT_CAPACITY = max_ingredients;
	}
	public CrucibleEntity() {
		super(AlchemicalBrewing.CRUCIBLE_BLOCK_ENTITY);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		MAX_WATER_CAPACITY = 9;
		MAX_INGREDIENT_CAPACITY = 16;
	}
	public void setLevel(int newLevel) {
		if (newLevel > level) {
			this.addLevels(newLevel - level, true);
		} else {
			this.takeLevels(level - newLevel, true);
		}
	}
	public int getLevel() {
		return level;
	}

	public int takeLevels(int amount, boolean takeLess) {
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
	public int addLevels(int amount, boolean addLess) {
		if (amount + level > this.MAX_WATER_CAPACITY) {
			if (addLess) {
				amount = this.MAX_WATER_CAPACITY - level;
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
		if (this.isReady() && this.itemsInPot.size() < this.MAX_INGREDIENT_CAPACITY) {
			this.add(itemsInPot, stack);
			markDirty();
			return true;
		}
		return false;
	}

	public void emptyPot() {
		this.itemsInPot.clear();
		this.level = 0;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
	}

	public boolean isReady() {
		return level > 0 && ticksToReady == 0;
	}

	public boolean isPureWater() {
		return this.itemsInPot.isEmpty();
	}

	@Override
	public void tick() {
		if (ticksToReady > 0) {
			ticksToReady -= 1;
			if (ticksToReady == 0) {
				AlchemicalBrewing.log(Level.INFO, "Crucible is ready");
			}
			markDirty();
		}
		if (isReady() != wasReady) {
			assert this.world != null;
			if (!this.world.isClient()) {
				this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.READY, this.isReady()), 2);
				this.wasReady = this.isReady();
			}
			markDirty();
		}
		if (isReady()) {
			for (CookingItemStack cookingItem: this.itemsInPot) {
				if (cookingItem.tickCooking()) {
					//TODO: check for complete recipes
				}
			}
		}
	}


	// Serialize the BlockEntity
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);

		// Save the current value of the number to the tag
		tag.putBoolean("wasReady", this.wasReady);
		tag.putInt("ticksToReady", this.ticksToReady);
		tag.putInt("level", this.level);
		
		ListTag listTag = new ListTag();

		for(int i = 0; i < this.itemsInPot.size(); ++i) {
			CookingItemStack cookingItem = (CookingItemStack)this.itemsInPot.get(i);
			CompoundTag compoundTag = new CompoundTag();
			cookingItem.itemStack.toTag(compoundTag);
			compoundTag.putInt("cookTimeRemaining", cookingItem.cookTimeRemaining);
			listTag.add(compoundTag);
		}

		if (!listTag.isEmpty()) {
			tag.put("Items", listTag);
		}

		return tag;
	}
	
	// Deserialize the BlockEntity
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		this.wasReady = tag.getBoolean("wasReady");
		this.ticksToReady = tag.getInt("ticksToReady");
		this.level = tag.getInt("level");
		
		ListTag listTag = tag.getList("Items", 10);

		for(int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			this.add(this.itemsInPot, ItemStack.fromTag(compoundTag), compoundTag.getInt("cookTimeRemaining"));
		}
	}
}
