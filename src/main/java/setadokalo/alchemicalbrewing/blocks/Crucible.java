package setadokalo.alchemicalbrewing.blocks;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.blocks.tileentity.CrucibleEntity;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.item.ABItems;
import setadokalo.alchemicalbrewing.item.FilledVial;

public class Crucible extends BaseEntityBlock {
	protected static final ParticleOptions PARTICLE = ParticleTypes.BUBBLE_COLUMN_UP;
	private static final VoxelShape RAY_TRACE_SHAPE = box(
		2.0D, 4.0D, 2.0D,
		14.0D, 16.0D, 14.0D);
	private static final VoxelShape OUTLINE_SHAPE = Shapes.join(
		Shapes.block(),
		Shapes.or(
			box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
			box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
			box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
			RAY_TRACE_SHAPE),
		BooleanOp.ONLY_FIRST);


	public static final BooleanProperty READY = BooleanProperty.create("ready");
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 9);


	public Crucible() {
		super(FabricBlockSettings.of(Material.STONE).hardness(4.0f).noOcclusion());
		registerDefaultState(getStateDefinition().any().setValue(READY, false).setValue(LEVEL, 0));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CrucibleEntity(pos, state, 9, 32);
	}


	private InteractionResult useOnEntity(Level world, BlockPos pos, ItemStack itemStack, Player player, InteractionHand hand, CrucibleEntity entity) {
		
		int i = entity.getWaterLevel();
		Item item = itemStack.getItem();
		if (item == Items.WATER_BUCKET && i < entity.maxWaterCapacity) {
			return useWaterBucket(world, pos, player, hand, entity);
		} else if (item == Items.POTION && PotionUtils.getPotion(itemStack) == Potions.WATER) {
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

	private InteractionResult useWaterBottle(Level world, BlockPos pos, Player player, InteractionHand hand, CrucibleEntity entity,
			int i) {
		if (i < entity.maxWaterCapacity && !world.isClientSide) {
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
			}

			entity.addLevels(1, true);
			world.playSound((Player)null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	private InteractionResult useWaterBucket(Level world, BlockPos pos, Player player, InteractionHand hand, CrucibleEntity entity) {
		if (!world.isClientSide) {
			if (!player.getAbilities().instabuild) {
				player.setItemInHand(hand, new ItemStack(Items.BUCKET));
			}

			entity.addLevels(9, true);
			world.playSound((Player) null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F,
					1.0F);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	// other items are consumed by the crucible
	private InteractionResult useOther(Level world, ItemStack itemStack, Player player, CrucibleEntity entity) {
		if (!world.isClientSide) {
			ItemStack borrowedStack = itemStack.copy();
			borrowedStack.setCount(1);
			if (entity.addItem(borrowedStack)) {
				if (!player.getAbilities().instabuild)
					itemStack.shrink(1);
				return InteractionResult.sidedSuccess(true);
			}
		}
		return InteractionResult.CONSUME;
	}

	private InteractionResult useFilledVial(Level world, BlockPos pos, ItemStack itemStack, Player player, InteractionHand hand,
			CrucibleEntity entity) {
		if (!world.isClientSide && entity.addLevels(1, false) == 1) {
			for (ConcentratedFluid effect : FilledVial.getFluids(itemStack))
				entity.addFluidToPot(effect);
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);		
				ItemStack emptyVial = new ItemStack(ABItems.VIAL, 1);
				if (itemStack.isEmpty()) {
					player.setItemInHand(hand, emptyVial);
				} else if (!player.getInventory().add(emptyVial)) {
					player.drop(emptyVial, false);
				}
			}
			world.playSound((Player)null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.sidedSuccess(true);
		}
		return InteractionResult.sidedSuccess(false);
	}

	private InteractionResult useVial(Level world, BlockPos pos, ItemStack itemStack, Player player, InteractionHand hand,
			CrucibleEntity entity, int i) {
		if (i >= 1 && !world.isClientSide) {
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}
			ItemStack newPotion = new ItemStack(ABItems.FILLED_VIAL, 1);
			newPotion.setTag(entity.takeLevels(1));
			if (itemStack.isEmpty()) {
				player.setItemInHand(hand, newPotion);
			} else if (!player.getInventory().add(newPotion)) {
				player.drop(newPotion, false);
			}
			world.playSound((Player)null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	private InteractionResult useBucket(Level world, BlockPos pos, ItemStack itemStack, Player player, InteractionHand hand,
			CrucibleEntity entity, int i) {
		if (i >= 9 && !world.isClientSide) {
			if (!player.getAbilities().instabuild && entity.isPureWater()) {
				itemStack.shrink(1);
				if (itemStack.isEmpty()) {
					player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
				} else if (!player.getInventory().add(new ItemStack(Items.WATER_BUCKET))) {
					player.drop(new ItemStack(Items.WATER_BUCKET), false);
				}
			}
			entity.removeLevels(9, true, false);
			world.playSound((Player)null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getItemInHand(hand);
		CrucibleEntity entity = (CrucibleEntity) world.getBlockEntity(pos);
		if (itemStack.isEmpty()) {
			if (player.isShiftKeyDown()) {
				entity.emptyPot();
			}
			return InteractionResult.PASS;
		} else {
			return useOnEntity(world, pos, itemStack, player, hand, entity);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return OUTLINE_SHAPE;
	}
	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
		return RAY_TRACE_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
		stateManager.add(READY).add(LEVEL);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		CrucibleEntity entity = (CrucibleEntity) world.getBlockEntity(pos);
		assert entity != null;
		if (state.getValue(READY)) {
			double surface = ((double)state.getValue(LEVEL)) / 16.0 + 0.4;
			double d = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
			double e = (double)pos.getY() + surface + (random.nextDouble() - 0.5D) * 0.15D;
			double f = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
			world.addParticle(ParticleTypes.BUBBLE_POP, d, e, f, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
      return (type == AlchemicalBrewing.crucibleBlockEntity) ? (w, bP, bS, entity) -> CrucibleEntity.tick(entity) : null;
	}
	
	// I despise you, Mojang.
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
  } 
}
