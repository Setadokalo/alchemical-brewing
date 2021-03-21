/* Code taken from the Pocket Tools mod under the MIT License

	The MIT License (MIT)

	Copyright (c) 2020 Emi

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

package setadokalo.alchemicalbrewing.mixin;

import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import setadokalo.alchemicalbrewing.AlchemicalBrewing;
import setadokalo.alchemicalbrewing.tooltip.ConvertibleTooltipData;

/**
 * This mixin is only required until FAPI adds ConvertibleTooltip, probably near to an official 1.17 release
 */
@Mixin(Screen.class)
public class ScreenMixin {
	
	@Inject(method = "lambda$renderTooltip$0", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private static void onComponentConstruct(List<ClientTooltipComponent> list, TooltipComponent data, CallbackInfo ci) {
		if (data instanceof ConvertibleTooltipData) {
			list.add(((ConvertibleTooltipData) data).getComponent());
			ci.cancel();
		}
	}
}