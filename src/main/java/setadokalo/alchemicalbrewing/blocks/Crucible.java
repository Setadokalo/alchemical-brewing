package setadokalo.alchemicalbrewing.blocks;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.item.ABItems;
import setadokalo.alchemicalbrewing.item.FilledVial;

public class Crucible extends BlockWithEntity {
	protected static final ParticleEffect PARTICLE = ParticleTypes.BUBBLE_COLUMN_UP;
	private static final VoxelShape RAY_TRACE_SHAPE = createCuboidShape(
		2.0D, 4.0D, 2.0D,
		14.0D, 16.0D, 14.0D);
	private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.combineAndSimplify(
		VoxelShapes.fullCube(),
		VoxelShapes.union(
			createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
			createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
			createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
			RAY_TRACE_SHAPE),
		BooleanBiFunction.ONLY_FIRST);


	public static final BooleanProperty READY = BooleanProperty.of("ready");
	public static final IntProperty LEVEL = IntProperty.of("level", 0, 9);


	public Crucible() {
		super(FabricBlockSettings.of(Material.STONE).hardness(4.0f).nonOpaque());
		setDefaultState(getStateManager().getDefaultState().with(READY, false).with(LEVEL, 0));
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CrucibleEntity(pos, state, 9, 32);
	}


	private ActionResult useOnEntity(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player, Hand hand, CrucibleEntity entity) {
		
		int i = entity.getLevel();
		Item item = itemStack.getItem();
		if (item == Items.WATER_BUCKET && i < entity.maxWaterCapacity) {
			return useWaterBucket(world, pos, player, hand, entity);
		} else if (item == Items.POTION && PotionUtil.getPotion(itemStack) == Potions.WATER) {
			return useWaterBottle(world, pos, player, hand, entity, i);
		} else if (item == Items.BUCKET) {
			return useBucket(world, pos, itemStack, player, hand, entity, i);
		} else if (item == ABItems.VIAL) {
			return useVial(world, pos, itemStack, player, hand, entity, i);
		} else if (item == ABItems.FILLED_VIAL) {
			return useFilledVial(world, pos, itemStack, player, hand, entity);
		} else {
			return useOther(world, itemStack, player, entity);
		}
	}

	private ActionResult useWaterBottle(World world, BlockPos pos, PlayerEntity player, Hand hand, CrucibleEntity entity,
			int i) {
		if (i < entity.maxWaterCapacity && !world.isClient) {
			if (!player.getAbilities().creativeMode) {
				player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
			}

			entity.addLevels(1, true);
			world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}

		return ActionResult.success(world.isClient);
	}

	private ActionResult useWaterBucket(World world, BlockPos pos, PlayerEntity player, Hand hand, CrucibleEntity entity) {
		if (!world.isClient) {
			if (!player.getAbilities().creativeMode) {
				player.setStackInHand(hand, new ItemStack(Items.BUCKET));
			}

			entity.addLevels(9, true);
			world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F,
					1.0F);
		}

		return ActionResult.success(world.isClient);
	}

	// other items are consumed by the crucible
	private ActionResult useOther(World world, ItemStack itemStack, PlayerEntity player, CrucibleEntity entity) {
		if (!world.isClient) {
			ItemStack borrowedStack = itemStack.copy();
			borrowedStack.setCount(1);
			if (entity.addItem(borrowedStack)) {
				if (!player.getAbilities().creativeMode)
					itemStack.decrement(1);
				return ActionResult.success(true);
			}
		}
		return ActionResult.CONSUME;
	}

	private ActionResult useFilledVial(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player, Hand hand,
			CrucibleEntity entity) {
		if (!world.isClient && entity.addLevels(1, false) == 1) {
			for (ConcentratedFluid effect : FilledVial.getFluids(itemStack))
				entity.addFluidToPot(effect);
			if (!player.getAbilities().creativeMode) {
				itemStack.decrement(1);		
				ItemStack emptyVial = new ItemStack(ABItems.VIAL, 1);
				if (itemStack.isEmpty()) {
					player.setStackInHand(hand, emptyVial);
				} else if (!player.getInventory().insertStack(emptyVial)) {
					player.dropItem(emptyVial, false);
				}
			}
			world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
			return ActionResult.success(true);
		}
		return ActionResult.success(false);
	}

	private ActionResult useVial(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player, Hand hand,
			CrucibleEntity entity, int i) {
		if (i >= 1 && !world.isClient) {
			if (!player.getAbilities().creativeMode) {
				itemStack.decrement(1);
			}
			ItemStack newPotion = new ItemStack(ABItems.FILLED_VIAL, 1);
			newPotion.setTag(entity.takeLevels(1));
			if (itemStack.isEmpty()) {
				player.setStackInHand(hand, newPotion);
			} else if (!player.getInventory().insertStack(newPotion)) {
				player.dropItem(newPotion, false);
			}
			world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}

		return ActionResult.success(world.isClient);
	}

	private ActionResult useBucket(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player, Hand hand,
			CrucibleEntity entity, int i) {
		if (i >= 9 && !world.isClient) {
			if (!player.getAbilities().creativeMode && entity.isPureWater()) {
				itemStack.decrement(1);
				if (itemStack.isEmpty()) {
					player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
				} else if (!player.getInventory().insertStack(new ItemStack(Items.WATER_BUCKET))) {
					player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
				}
			}
			entity.removeLevels(9, true, false);
			world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}

		return ActionResult.success(world.isClient);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getStackInHand(hand);
		CrucibleEntity entity = (CrucibleEntity) world.getBlockEntity(pos);
		if (itemStack.isEmpty()) {
			if (player.isSneaking()) {
				entity.emptyPot();
			}
			return ActionResult.PASS;
		} else {
			return useOnEntity(world, pos, itemStack, player, hand, entity);
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE_SHAPE;
	}
	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return RAY_TRACE_SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
		stateManager.add(READY).add(LEVEL);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		CrucibleEntity entity = (CrucibleEntity) world.getBlockEntity(pos);
		assert entity != null;
		if (state.get(READY)) {
			double surface = ((double)state.get(LEVEL)) / 16.0 + 0.4;
			double d = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
			double e = (double)pos.getY() + surface + (random.nextDouble() - 0.5D) * 0.15D;
			double f = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
			world.addParticle(ParticleTypes.BUBBLE_POP, d, e, f, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return (type == AlchemicalBrewing.crucibleBlockEntity) ? (w, bP, bS, entity) -> CrucibleEntity.tick(entity) : null;
	}
	
	// I despise you, Mojang.
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
  } 
}
