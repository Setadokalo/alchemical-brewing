package setadokalo.alchemicalbrewing.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.config.ABConfigScreen;

@Mixin(AbstractInventoryScreen.class)
public class InventoryScreenMixin extends Screen {

	protected InventoryScreenMixin(Text title) {
		super(title);
	}
	@Inject(method = "init()V", at = @At("RETURN"))
	private void addButton(CallbackInfo ci) {
		this.addButton(new ButtonWidget(10, 10, 100, 20, new LiteralText("AB Settings"), btn -> {
			assert this.client != null;
			this.client.openScreen(new ABConfigScreen(this));
		}));
	}
}
