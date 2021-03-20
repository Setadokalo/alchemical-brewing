package setadokalo.alchemicalbrewing.item;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.config.ABConfig.TooltipMode;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.tooltip.ConvertibleTooltipData;
import setadokalo.alchemicalbrewing.util.Color;

public class FilledVial extends Item {

	public FilledVial() {
		super(new Settings().group(AlchemicalBrewing.ITEM_GROUP).maxCount(16));
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.DRINK;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 32;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		List<ConcentratedFluid> effects = getFluids(stack);
		BigFraction total = BigFraction.ZERO;
		for (ConcentratedFluid effect : effects) {
			tooltip.add(effect.getTooltip());
			total = total.add(effect.concentration);
		}
		if (!total.equals(BigFraction.ZERO) && (total.compareTo(new BigFraction(1, 32)) < 0)) {
			tooltip.add(new TranslatableText("tooltip.alchemicalbrewing.homeopathy").formatted(Formatting.LIGHT_PURPLE).formatted(Formatting.ITALIC));
			tooltip.add(new TranslatableText("tooltip.alchemicalbrewing.homeopathy2").formatted(Formatting.LIGHT_PURPLE).formatted(Formatting.ITALIC));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (user.canConsume(true)) {
			user.setCurrentHand(hand);
			return TypedActionResult.consume(user.getStackInHand(hand));
		} else {
			return TypedActionResult.fail(user.getStackInHand(hand));
		}
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		List<ConcentratedFluid> effects = getFluids(stack);
		if (!world.isClient && user instanceof PlayerEntity && !((PlayerEntity) user).getAbilities().creativeMode) {
			stack.decrement(1);
			((PlayerEntity) user).giveItemStack(new ItemStack(ABItems.VIAL, 1));
		}

		for (ConcentratedFluid cEffect : effects) {
			cEffect.applyEffects(world, user);
		}
		return stack;
	}

	public static List<ConcentratedFluid> getFluids(ItemStack stack) {
		return getFluidsForTag(stack.getTag());
	}

	public static List<ConcentratedFluid> getFluidsForTag(@Nullable NbtCompound tag) {
		List<ConcentratedFluid> list = Lists.newArrayList();
		getFluidsForTag(tag, list);
		return list;
	}

	public static void getFluidsForTag(NbtCompound tag, List<ConcentratedFluid> list) {
		if (tag != null && tag.contains("Effects", 9)) {
			NbtList listTag = tag.getList("Effects", 10);

			for (int i = 0; i < listTag.size(); ++i) {
				NbtCompound compoundTag = listTag.getCompound(i);
				ConcentratedFluid effect = ConcentratedFluid.fromTag(compoundTag);
				if (effect != null) {
					list.add(effect);
				}
			}
		}
	}

	public static NbtList getTagForFluids(ConcentratedFluid... fluids) {
		NbtList fluidList = new NbtList();
		for (ConcentratedFluid fluid : fluids) {
			NbtCompound tag = new NbtCompound();
			fluid.writeNbt(tag);
			fluidList.add(tag);
		}
		return fluidList;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new VialTooltip(stack));
		
	}

	static class VialTooltip implements ConvertibleTooltipData, TooltipComponent {
		public ItemStack stack;
		public List<ConcentratedFluid> fluids;

		public VialTooltip(ItemStack stack) {
			this.stack = stack;
			fluids = getFluids(stack);
		}

		@Override
		public int getHeight() {
			if (AlchemicalBrewing.config.tooltipMode.equals(TooltipMode.BAR)) {
				return 12;
			} else {
				return 12 * fluids.size() + 2;
			}
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return BAR_LENGTH + 2;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}
		
		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, 
				ItemRenderer itemRenderer, int z, TextureManager textureManager) 
		{
			itemRenderer.renderItem(new ItemStack(Items.STONE, 3), ModelTransformation.Mode.GUI, 1, 0, matrices, x, y);
			if (AlchemicalBrewing.config.tooltipMode.equals(TooltipMode.CIRCLES))
				drawCircles(x, y, matrices);
			else
				drawBar(x, y, matrices, textureManager);
		}

		public static final int BAR_LENGTH = 128;
		private void drawBar(int x, int y, MatrixStack matrices, TextureManager textureManager) {
			matrices.push();
			matrices.translate(x, y, 0.0);
			RenderSystem.enableBlend();
			RenderSystem.enableTexture();
			RenderSystem.enableColorLogicOp();
			RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 1.0f);
			RenderSystem.defaultBlendFunc();
//			RenderSystem.enableCull();
			// both bar renders use the same texture
			textureManager.bindTexture(new Identifier(AlchemicalBrewing.MODID, "textures/gui/bars.png"));
			float barDeltaY = 5.0F / 16.0F; // The distance in texture space of a single bar in the texture
//			RenderSystem.enableAlphaTest();
			// first, we render the background bar
			{
				Matrix4f matrix = matrices.peek().getModel();
	
				BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
	
				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
				
				bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F)
					.color(255, 255, 255, 255)
					.texture(0.0F, 0.0F)
					.light(0, 0)
					.next();
				bufferBuilder.vertex(matrix, 0.0F, 10.0F, 0.0F)
					.color(255, 255, 255, 255)
					.texture(0.0F, barDeltaY)
					.light(0, 1)
					.next();
				bufferBuilder.vertex(matrix, (float)BAR_LENGTH, 10.0F, 0.0F)
					.color(255, 255, 255, 255)
					.texture(1.0F, barDeltaY)
					.light(1, 1)
					.next();
				bufferBuilder.vertex(matrix, (float)BAR_LENGTH, 0.0F, 0.0F)
					.color(255, 255, 255, 255)
					.texture(1.0F, 0.0F)
					.light(1, 0)
					.next();
				
				bufferBuilder.end();
				BufferRenderer.draw(bufferBuilder);
			}
			// Then, we render each fluid in the vial as a fraction of a foreground bar
			double currentTotal = 0.0;
			double barLengthScaled = ((double)BAR_LENGTH) / 10.0; //TODO: 10.0 should be a constant (max capacity of vial)

//			for (ConcentratedFluid fluid : fluids) {
//				Color fluidColor = fluid.fluid.getColor(stack);
//
//				Matrix4f matrix = matrices.peek().getModel();
//
//				BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
//
//				double oldTotal = currentTotal;
//				currentTotal = Math.min(currentTotal + fluid.concentration.doubleValue(), 10.0);
//				bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
//				bufferBuilder.vertex(matrix, (float) (oldTotal * barLengthScaled), 0.0F, 0.0F)
//					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
//					.texture((float)(oldTotal / 10.0), barDeltaY)
//					.next();
//				bufferBuilder.vertex(matrix, (float) (oldTotal * barLengthScaled), 10.0F, 0.0F)
//					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
//					.texture((float)(oldTotal / 10.0), barDeltaY * 2.0F)
//					.next();
//				bufferBuilder.vertex(matrix, (float) (currentTotal * barLengthScaled), 10.0F, 0.0F)
//					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
//					.texture((float)(currentTotal / 10.0), barDeltaY * 2.0F)
//					.next();
//				bufferBuilder.vertex(matrix, (float) (currentTotal * barLengthScaled), 0.0F, 0.0F)
//					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
//					.texture((float)(currentTotal / 10.0), barDeltaY)
//					.next();
//
//				bufferBuilder.end();
//				BufferRenderer.draw(bufferBuilder);
//
//			}
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.disableColorLogicOp();
			matrices.pop();
		}

		private void drawCircles(int x, int y, MatrixStack matrices) {
			for (int i = 0; i < fluids.size(); i++) {
				ConcentratedFluid fluid = fluids.get(i);
				Color fluidColor = fluid.fluid.getColor(stack);
				int maxVerticesInCircle = 31;
				double fracAmount = fluid.concentration.doubleValue() - (double)fluid.concentration.intValue();
				int verticesInCircle = (int)Math.ceil(((double)maxVerticesInCircle) * fracAmount);

				matrices.push();
				matrices.translate(x + 6, y + (14 * i) + 6, 0);
				// matrices.scale(8.0F, -8.0F, 1.0F);
				int wholeAmount = fluid.concentration.intValue();
				for (int w = 0; w < wholeAmount; w++) {
					Matrix4f matrix = matrices.peek().getModel();

					BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
					RenderSystem.enableBlend();
					RenderSystem.disableTexture();
					RenderSystem.defaultBlendFunc();
					bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
					bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
					bufferBuilder.vertex(matrix, 0.0F, -6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
					for (int v = 1; v <= maxVerticesInCircle; v++) {
						double circleCoord = ((double) v) / ((double) maxVerticesInCircle) * Math.PI * 2.0;
						bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F)
						.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
					}
					// bufferBuilder.vertex(matrix, -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).next();
					bufferBuilder.end();
					BufferRenderer.draw(bufferBuilder);
					RenderSystem.enableTexture();
					RenderSystem.disableBlend();
					
					matrices.translate(14.0, 0.0, 0.0);

				}

				Matrix4f matrix = matrices.peek().getModel();

				BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
				RenderSystem.enableBlend();
				RenderSystem.disableTexture();
				RenderSystem.defaultBlendFunc();
				bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
				bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
				bufferBuilder.vertex(matrix, 0.0F, -6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
				for (int v = 1; v < verticesInCircle; v++) {
					double circleCoord = ((double) v) / ((double) maxVerticesInCircle) * Math.PI * 2.0;
					bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
				}
				double circleCoord = fracAmount * Math.PI * 2.0;
				bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).next();
				// bufferBuilder.vertex(matrix, -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).next();
				bufferBuilder.end();
				BufferRenderer.draw(bufferBuilder);
				RenderSystem.enableTexture();
				RenderSystem.disableBlend();

				matrices.pop();
			}
		}
	}
}
