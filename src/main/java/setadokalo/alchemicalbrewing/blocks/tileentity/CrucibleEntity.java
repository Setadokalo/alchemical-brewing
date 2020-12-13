package setadokalo.alchemicalbrewing.blocks.tileentity;

import org.apache.logging.log4j.Level;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.Tickable;

import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.Crucible;

public class CrucibleEntity extends BlockEntity implements Tickable {

	/** The number of ticks until the crucible is ready to be used. */
	private boolean wasReady = true;
	private int ticksToReady = 0;
	/** The maximum capacity of this crucible (allowing this one base class to be reused). */
	public final int MAX_CAPACITY;
	/** The current number of bottles of water in this crucible. One bucket = 8 bottles, because how TF does
	  * one bottle contain a *third of a cubic meter* of water, Mojang? */
	private int level = 0;

	private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
		@Override
		public int get(int index) {
			switch (index) {
				case 0:
					return level;
				case 1:
					return ticksToReady;
				default:
					throw new IllegalArgumentException();
			}
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0:
					level = value;
					break;
				case 1:
					ticksToReady = value;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		//this is supposed to return the amount of integers you have in your delegate, in our example only one
		@Override
		public int size() {
			return 2;
		}
	};

	public CrucibleEntity(int max_capacity) {
		super(AlchemicalBrewing.CRUCIBLE_BLOCK_ENTITY);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		MAX_CAPACITY = max_capacity;
	}
	public CrucibleEntity() {
		super(AlchemicalBrewing.CRUCIBLE_BLOCK_ENTITY);
		AlchemicalBrewing.log(Level.INFO, "created a crucible entity");
		MAX_CAPACITY = 8;
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
		return amount;
	}
	public int addLevels(int amount, boolean addLess) {
		if (amount + level > 8) {
			if (addLess) {
				amount = 8 - level;
			} else {
				return 0;
			}
		}
		ticksToReady += 20 * amount;
		level = level + amount;
		AlchemicalBrewing.log(Level.INFO, String.format("Crucible now has %d levels.", level));
		assert this.world != null;
		this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.LEVEL, level), 2);
		return amount;
	}

	public boolean isReady() {
		return level > 0 && ticksToReady == 0;
	}

	public boolean isPureWater() {
		return true; // TODO: implement potion brewing so the water can become non-pure
	}

	@Override
	public void tick() {
		if (ticksToReady > 0) {
			ticksToReady -= 1;
			if (ticksToReady == 0) {
				AlchemicalBrewing.log(Level.INFO, "Crucible is ready");
			}
		}
		if (isReady() != wasReady) {
			assert this.world != null;
			if (!this.world.isClient()) {
				this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(Crucible.READY, this.isReady()), 2);
				this.wasReady = this.isReady();
			}
		}
		//TODO: Tick the potion brew here
	}
}
