package setadokalo.alchemicalbrewing.item;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.commons.math3.fraction.BigFraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.config.ABConfig.TooltipMode;
import setadokalo.alchemicalbrewing.fluideffects.ConcentratedFluid;
import setadokalo.alchemicalbrewing.tooltip.ConvertibleTooltipData;
import setadokalo.alchemicalbrewing.util.Color;

import javax.annotation.ParametersAreNonnullByDefault;

public class FilledVial extends Item {

	public FilledVial() {
		super(new Properties().tab(AlchemicalBrewing.ITEM_GROUP).stacksTo(16));
	}

	@Override
	public UseAnim getUseAnimation(@NotNull ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(@NotNull ItemStack stack) {
		return 32;
	}

	@Override
	@Environment(EnvType.CLIENT)
	@ParametersAreNonnullByDefault
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		List<ConcentratedFluid> effects = getFluids(stack);
		BigFraction total = BigFraction.ZERO;
		for (ConcentratedFluid effect : effects) {
			tooltip.add(effect.getTooltip());
			total = total.add(effect.concentration);
		}
		if (!total.equals(BigFraction.ZERO) && (total.compareTo(new BigFraction(1, 32)) < 0)) {
			tooltip.add(new TranslatableComponent("tooltip.alchemicalbrewing.homeopathy").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
			tooltip.add(new TranslatableComponent("tooltip.alchemicalbrewing.homeopathy2").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
		}
	}

	@Override
	@ParametersAreNonnullByDefault
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		if (user.canEat(true)) {
			user.startUsingItem(hand);
			return InteractionResultHolder.consume(user.getItemInHand(hand));
		} else {
			return InteractionResultHolder.fail(user.getItemInHand(hand));
		}
	}

	@Override
	public ItemStack finishUsingItem(@NotNull ItemStack stack, Level world, @NotNull LivingEntity user) {
		List<ConcentratedFluid> effects = getFluids(stack);
		if (!world.isClientSide && user instanceof Player player && !player.getAbilities().instabuild) {
			stack.shrink(1);
			player.addItem(new ItemStack(ABItems.VIAL, 1));
		}

		for (ConcentratedFluid cEffect : effects) {
			cEffect.applyEffects(world, user);
		}
		return stack;
	}

	public static List<ConcentratedFluid> getFluids(ItemStack stack) {
		return getFluidsForTag(stack.getTag());
	}

	public static List<ConcentratedFluid> getFluidsForTag(@Nullable CompoundTag tag) {
		List<ConcentratedFluid> list = Lists.newArrayList();
		getFluidsForTag(tag, list);
		return list;
	}

	public static void getFluidsForTag(CompoundTag tag, List<ConcentratedFluid> list) {
		if (tag != null && tag.contains("Effects", 9)) {
			ListTag listTag = tag.getList("Effects", 10);

			for (int i = 0; i < listTag.size(); ++i) {
				CompoundTag compoundTag = listTag.getCompound(i);
				ConcentratedFluid effect = ConcentratedFluid.fromTag(compoundTag);
				if (effect != null) {
					list.add(effect);
				}
			}
		}
	}

	public static ListTag getTagForFluids(ConcentratedFluid... fluids) {
		ListTag fluidList = new ListTag();
		for (ConcentratedFluid fluid : fluids) {
			CompoundTag tag = new CompoundTag();
			fluid.writeNbt(tag);
			fluidList.add(tag);
		}
		return fluidList;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
		return Optional.of(new VialTooltip(stack));
	}

	static class VialTooltip implements ConvertibleTooltipData, ClientTooltipComponent {
		public ItemStack stack;
		public List<ConcentratedFluid> fluids;

		public VialTooltip(ItemStack stack) {
			this.stack = stack;
			fluids = getFluids(stack);
		}

		@Override
		public int getHeight() {
			if (AlchemicalBrewing.config.tooltipMode.equals(TooltipMode.BAR)) {
				return 7;
			} else {
				return 12 * fluids.size() + 2;
			}
		}

		@Override
		public int getWidth(Font textRenderer) {
			return BAR_LENGTH + 2;
		}

		@Override
		public ClientTooltipComponent getComponent() {
			return this;
		}
		
		@Override
		public void renderImage(Font textRenderer, int x, int y, PoseStack matrices, 
				ItemRenderer itemRenderer, int z, TextureManager textureManager) 
		{
			if (AlchemicalBrewing.config.tooltipMode.equals(TooltipMode.CIRCLES))
				drawCircles(x, y, matrices);
			else
				drawBar(x, y, matrices, textureManager);
		}

		public static final int BAR_LENGTH = 128;
		private void drawBar(int x, int y, PoseStack matrices, TextureManager textureManager) {
			matrices.pushPose();
			matrices.translate(x, y, 0.0);
			RenderSystem.enableBlend();
			RenderSystem.enableTexture();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.defaultBlendFunc();
			// both bar renders use the same texture
			RenderSystem.setShaderTexture(0, new ResourceLocation(AlchemicalBrewing.MODID, "textures/gui/bars.png"));
			float barDeltaY = 5.0F / 16.0F; // The distance in texture space of a single bar in the texture
			// first, we render the background bar
			{
				Matrix4f matrix = matrices.last().pose();
	
				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
	
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
				
				bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F)
					.uv(0.0F, 0.0F)
					.color(255, 255, 255, 255)
					.endVertex();
				bufferBuilder.vertex(matrix, 0.0F, 5.0F, 0.0F)
					.uv(0.0F, barDeltaY)
					.color(255, 255, 255, 255)
					.endVertex();
				bufferBuilder.vertex(matrix, (float)BAR_LENGTH, 5.0F, 0.0F)
					.uv(1.0F, barDeltaY)
					.color(255, 255, 255, 255)
					.endVertex();
				bufferBuilder.vertex(matrix, (float)BAR_LENGTH, 0.0F, 0.0F)
					.uv(1.0F, 0.0F)
					.color(255, 255, 255, 255)
					.endVertex();
				
				bufferBuilder.end();
				BufferUploader.end(bufferBuilder);
			}
			// Then, we render each fluid in the vial as a fraction of a foreground bar
			double currentTotal = 0.0;
			double barLengthScaled = ((double)BAR_LENGTH) / 10.0; //TODO: 10.0 should be a constant (max capacity of vial)

			for (ConcentratedFluid fluid : fluids) {
				Color fluidColor = fluid.fluid.getColor(stack);

				Matrix4f matrix = matrices.last().pose();

				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

				double oldTotal = currentTotal;
				currentTotal = Math.min(currentTotal + fluid.concentration.doubleValue(), 10.0);
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
				bufferBuilder.vertex(matrix, (float) (oldTotal * barLengthScaled), 0.0F, 0.0F)
					.uv((float)(oldTotal / 10.0), barDeltaY)
					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
					.endVertex();
				bufferBuilder.vertex(matrix, (float) (oldTotal * barLengthScaled), 5.0F, 0.0F)
					.uv((float)(oldTotal / 10.0), barDeltaY * 2.0F)
					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
					.endVertex();
				bufferBuilder.vertex(matrix, (float) (currentTotal * barLengthScaled), 5.0F, 0.0F)
					.uv((float)(currentTotal / 10.0), barDeltaY * 2.0F)
					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
					.endVertex();
				bufferBuilder.vertex(matrix, (float) (currentTotal * barLengthScaled), 0.0F, 0.0F)
					.uv((float)(currentTotal / 10.0), barDeltaY)
					.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255)
					.endVertex();

				bufferBuilder.end();
				BufferUploader.end(bufferBuilder);

			}
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.disableColorLogicOp();
			matrices.popPose();
		}

		private void drawCircles(int x, int y, PoseStack matrices) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.enableBlend();
			RenderSystem.disableTexture();
			RenderSystem.defaultBlendFunc();
			for (int i = 0; i < fluids.size(); i++) {
				ConcentratedFluid fluid = fluids.get(i);
				Color fluidColor = fluid.fluid.getColor(stack);
				int maxVerticesInCircle = 31;
				double fracAmount = fluid.concentration.doubleValue() - (double)fluid.concentration.intValue();
				int verticesInCircle = (int)Math.ceil(((double)maxVerticesInCircle) * fracAmount);

				matrices.pushPose();
				matrices.translate(x + 6, y + (14 * i) + 6, 0);
				// matrices.scale(8.0F, -8.0F, 1.0F);
				int wholeAmount = fluid.concentration.intValue();
				for (int w = 0; w < wholeAmount; w++) {
					Matrix4f matrix = matrices.last().pose();

					BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
					bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
					bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
					bufferBuilder.vertex(matrix, 0.0F, -6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
					for (int v = 1; v <= maxVerticesInCircle; v++) {
						double circleCoord = ((double) v) / ((double) maxVerticesInCircle) * Math.PI * 2.0;
						bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F)
						.color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
					}
					// bufferBuilder.vertex(matrix, -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).next();
					bufferBuilder.end();
					BufferUploader.end(bufferBuilder);
					
					matrices.translate(14.0, 0.0, 0.0);

				}

				Matrix4f matrix = matrices.last().pose();

				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
				bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
				bufferBuilder.vertex(matrix, 0.0F, -6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
				for (int v = 1; v < verticesInCircle; v++) {
					double circleCoord = ((double) v) / ((double) maxVerticesInCircle) * Math.PI * 2.0;
					bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
				}
				double circleCoord = fracAmount * Math.PI * 2.0;
				bufferBuilder.vertex(matrix, -(float)Math.sin(circleCoord) * 6.0F, -(float)Math.cos(circleCoord) * 6.0F, 0.0F).color(fluidColor.getRed(), fluidColor.getGreen(), fluidColor.getBlue(), 255).endVertex();
				// bufferBuilder.vertex(matrix, -1.0F, 0.0F, 0.0F).color(255, 255, 255, 255).next();
				bufferBuilder.end();
				BufferUploader.end(bufferBuilder);

				matrices.popPose();
			}
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
		}
	}
}
