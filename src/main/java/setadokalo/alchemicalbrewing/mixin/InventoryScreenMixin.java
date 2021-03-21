package setadokalo.alchemicalbrewing.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.config.ABConfigScreen;

@Mixin(EffectRenderingInventoryScreen.class)
public class InventoryScreenMixin extends Screen {

	protected InventoryScreenMixin(Component title) {
		super(title);
	}
	@Inject(method = "init()V", at = @At("RETURN"))
	private void addButton(CallbackInfo ci) {
		this.addButton(new Button(10, 10, 100, 20, new TextComponent("AB Settings"), btn -> {
			assert this.minecraft != null;
			this.minecraft.setScreen(new ABConfigScreen(this));
		}));
	}
}
