package setadokalo.alchemicalbrewing.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;

public class ABConfigScreen extends Screen {

	Screen parent;

	public ABConfigScreen(@Nullable Screen parent) {
		super(new TranslatableComponent("title.alchemicalbrewing.config"));
		this.parent = parent;	
	}
	
	@Override
	protected void init() {
		super.init();
		int columnX = this.width / 2 - 100;
		String currentState = "button.alchemicalbrewing.tooltipmode." + 
			(AlchemicalBrewing.config.tooltipMode.equals(ABConfig.TooltipMode.CIRCLES) ? "circle" : "bar"); 
		this.addButton(
			new Button(
				columnX, 20, 
				Math.min(200, this.width - (columnX + 5)), 20, 
				new TranslatableComponent(currentState),
				btn -> {
					if (AlchemicalBrewing.config.tooltipMode.equals(ABConfig.TooltipMode.CIRCLES)) {
						AlchemicalBrewing.config.tooltipMode = ABConfig.TooltipMode.BAR;
						btn.setMessage(new TranslatableComponent("button.alchemicalbrewing.tooltipmode.bar"));
					} else {
						AlchemicalBrewing.config.tooltipMode = ABConfig.TooltipMode.CIRCLES;
						btn.setMessage(new TranslatableComponent("button.alchemicalbrewing.tooltipmode.circle"));
					}
				}));
		this.addButton(
			new Button(
				columnX, this.height - 29, 
				Math.min(200, this.width - (columnX + 5)), 20, 
				new TranslatableComponent("button.alchemicalbrewing.saveandquit"),
				btn -> {
					AlchemicalBrewing.config.saveConfig();
					this.minecraft.setScreen(this.parent);
				}));
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta)
	{
		 this.renderBackground(matrices);
		 super.render(matrices, mouseX, mouseY, delta);
		 // Draw the title text.
		 drawCenteredString(matrices, this.font, this.title, this.width / 2, 8, 0xFFFFFF);

	}
}
