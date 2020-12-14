package setadokalo.alchemicalbrewing.blocks;

import java.util.Random;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
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

public class Crucible extends Block implements BlockEntityProvider {
	protected final ParticleEffect particle = ParticleTypes.BUBBLE_COLUMN_UP;
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
	public @Nullable BlockEntity createBlockEntity(BlockView world) {
		return new CrucibleEntity(9, 16);
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getStackInHand(hand);
		CrucibleEntity entity = (CrucibleEntity) world.getBlockEntity(pos);
		if (itemStack.isEmpty()) {
			if (player.isSneaking()) {
				entity.emptyPot();
			}
			return ActionResult.PASS;
		} else {
			int i = entity.getLevel();
			Item item = itemStack.getItem();
			if (item == Items.WATER_BUCKET && i < entity.MAX_WATER_CAPACITY) {
				if (!world.isClient) {
					if (!player.abilities.creativeMode) {
						player.setStackInHand(hand, new ItemStack(Items.BUCKET));
					}

					entity.addLevels(9, true);
					world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F,
							1.0F);
				}

				return ActionResult.success(world.isClient);
			} else if (item == Items.POTION && PotionUtil.getPotion(itemStack) == Potions.WATER) {
				if (i < entity.MAX_WATER_CAPACITY && !world.isClient) {
					if (!player.abilities.creativeMode) {
						player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
					}

					entity.addLevels(1, true);
					world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}

				return ActionResult.success(world.isClient);
			} else if (item == Items.BUCKET) {
				if (i >= 9 && !world.isClient) {
					if (!player.abilities.creativeMode) {
						itemStack.decrement(1);
						if (itemStack.isEmpty()) {
							if (entity.isPureWater()) {
								player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
							}
						} else if (!player.inventory.insertStack(new ItemStack(Items.WATER_BUCKET))) {
							player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
						}
					}
					entity.takeLevels(9, true);
					world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}

				return ActionResult.success(world.isClient);
			} else if (item == Items.GLASS_BOTTLE) {
				if (i >= 1 && !world.isClient) {
					if (!player.abilities.creativeMode) {
						itemStack.decrement(1);
						if (itemStack.isEmpty()) {
							if (entity.isPureWater()) {
								player.setStackInHand(hand, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
							}
						} else if (!player.inventory.insertStack(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER))) {
							player.dropItem(Items.POTION.getDefaultStack(), false);
						}
					}
					entity.takeLevels(1, true);
					world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}

				return ActionResult.success(world.isClient);
			} else {
				if (!world.isClient) {
					ItemStack borrowedStack = itemStack.copy();
					borrowedStack.setCount(1);
					if (entity.addItem(itemStack)) {
						if (!player.abilities.creativeMode)
							itemStack.decrement(1);
					}
				}
				return ActionResult.SUCCESS;
			}
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
}
