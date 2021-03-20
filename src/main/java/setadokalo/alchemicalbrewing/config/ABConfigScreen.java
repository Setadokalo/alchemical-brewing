package setadokalo.alchemicalbrewing.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.TextureManager;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class ABConfigScreen extends Screen {

	Screen parent;

	public ABConfigScreen(@Nullable Screen parent) {
		super(new TranslatableText("title.alchemicalbrewing.config"));
		this.parent = parent;	
	}
	
	@Override
	protected void init() {
		super.init();
		int columnX = this.width / 2 - 100;
		String currentState = "button.alchemicalbrewing.tooltipmode." + 
			(AlchemicalBrewing.config.tooltipMode.equals(ABConfig.TooltipMode.CIRCLES) ? "circle" : "bar"); 
		this.addButton(
			new ButtonWidget(
				columnX, 20, 
				Math.min(200, this.width - (columnX + 5)), 20, 
				new TranslatableText(currentState),
				btn -> {
					if (AlchemicalBrewing.config.tooltipMode.equals(ABConfig.TooltipMode.CIRCLES)) {
						AlchemicalBrewing.config.tooltipMode = ABConfig.TooltipMode.BAR;
						btn.setMessage(new TranslatableText("button.alchemicalbrewing.tooltipmode.bar"));
					} else {
						AlchemicalBrewing.config.tooltipMode = ABConfig.TooltipMode.CIRCLES;
						btn.setMessage(new TranslatableText("button.alchemicalbrewing.tooltipmode.circle"));
					}
				}));
		this.addButton(
			new ButtonWidget(
				columnX, this.height - 29, 
				Math.min(200, this.width - (columnX + 5)), 20, 
				new TranslatableText("button.alchemicalbrewing.saveandquit"),
				btn -> {
					AlchemicalBrewing.config.saveConfig();
					this.client.openScreen(this.parent);
				}));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		 this.renderBackground(matrices);
		 super.render(matrices, mouseX, mouseY, delta);
		 // Draw the title text.
		 drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);

	}
}
