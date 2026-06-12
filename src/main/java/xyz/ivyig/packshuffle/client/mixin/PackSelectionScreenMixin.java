package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.client.PackShuffleConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.packs.PackSelectionScreen")
public abstract class PackSelectionScreenMixin extends Screen {
    @Shadow private Button doneButton;

    private Button settingsButton;

    protected PackSelectionScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.settingsButton = Button.builder(
            Component.literal("PackShuffle Settings"),
            btn -> Minecraft.getInstance().setScreen(new PackShuffleConfigScreen(this))
        ).bounds(8, this.height - 28, 130, 20).build();

        this.addRenderableWidget(this.settingsButton);
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void onRepositionElements(CallbackInfo ci) {
        if (this.settingsButton != null && this.doneButton != null) {
            this.settingsButton.setY(this.doneButton.getY());
            this.settingsButton.setHeight(this.doneButton.getHeight());
        }
    }
}
