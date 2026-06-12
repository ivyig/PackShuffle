package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.client.PackShuffleConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screen.pack.PackScreen")
public abstract class PackSelectionScreenMixin extends Screen {
    @Shadow private ButtonWidget doneButton;

    private ButtonWidget settingsButton;

    protected PackSelectionScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.settingsButton = ButtonWidget.builder(
            Text.literal("PackShuffle Settings"),
            btn -> MinecraftClient.getInstance().setScreen(new PackShuffleConfigScreen(this))
        ).dimensions(8, this.height - 28, 130, 20).build();

        this.addDrawableChild(this.settingsButton);
    }

    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void onRefreshWidgetPositions(CallbackInfo ci) {
        if (this.settingsButton != null && this.doneButton != null) {
            this.settingsButton.setY(this.doneButton.getY());
            this.settingsButton.setHeight(this.doneButton.getHeight());
        }
    }
}
