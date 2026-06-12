package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screen.pack.PackListWidget$ResourcePackEntry")
public abstract class PackEntryMixin {
    @Shadow @Final protected MinecraftClient client;
    @Shadow @Final private ResourcePackOrganizer.Pack pack;

    @Shadow @Final private TextWidget nameWidget;
    @Shadow @Final private MultilineTextWidget descriptionWidget;

    private boolean normalPack;
    private boolean isShuffleSlot;
    private String packId;
    private boolean favorite;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(PackListWidget parentList, MinecraftClient client, PackListWidget widget, ResourcePackOrganizer.Pack pack, CallbackInfo ci) {
        if (pack != null) {
            this.packId = pack.getName();
            this.normalPack = this.packId != null && this.packId.startsWith("file/");
            this.isShuffleSlot = "file/packshuffle_slot".equals(this.packId);
            if (this.normalPack && !this.isShuffleSlot) {
                this.favorite = Config.load().favorites.contains(this.packId);
            }
        }
        if (!this.normalPack || this.isShuffleSlot) {
            return;
        }
        if (this.nameWidget != null) {
            this.nameWidget.setWidth(this.nameWidget.getWidth() - 20);
        }
        if (this.descriptionWidget != null) {
            this.descriptionWidget.setWidth(this.descriptionWidget.getWidth() - 20);
        }
    }

    private boolean isNormalPack() {
        return this.normalPack;
    }

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/TextWidget;setMaxWidth(I)Lnet/minecraft/client/gui/widget/TextWidget;"
        ),
        index = 0
    )
    private int modifyNameMaxWidth(int maxWidth) {
        if (!this.normalPack || this.isShuffleSlot) {
            return maxWidth;
        }
        return maxWidth - 20;
    }

    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/MultilineTextWidget;setMaxWidth(I)Lnet/minecraft/client/gui/widget/MultilineTextWidget;"
        ),
        index = 0
    )
    private int modifyDescriptionMaxWidth(int maxWidth) {
        if (!this.normalPack || this.isShuffleSlot) {
            return maxWidth;
        }
        return maxWidth - 20;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (!this.normalPack || this.isShuffleSlot) {
            return;
        }
        PackListWidget.Entry entry = (PackListWidget.Entry) (Object) this;
        int right = entry.getContentRightEnd();
        int top = entry.getContentY();

        int starX = right - 16;
        int starY = top + 10;

        boolean starHovered = mouseX >= starX && mouseX <= starX + 12 && mouseY >= starY && mouseY <= starY + 12;

        String starChar = this.favorite ? "★" : (hovered ? "☆" : "");
        int starColor = this.favorite ? 0xFFFFD700 : (starHovered ? 0xFFFFFFFF : 0xFF888888);

        context.drawText(client.textRenderer, starChar, starX + 2, starY + 1, starColor, false);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!this.normalPack || this.isShuffleSlot) {
            return;
        }
        PackListWidget.Entry entry = (PackListWidget.Entry) (Object) this;
        int clickX = (int) click.x();
        int clickY = (int) click.y();
        int right = entry.getContentRightEnd();
        int top = entry.getContentY();

        int starX = right - 16;
        int starY = top + 10;

        if (clickX >= starX && clickX <= starX + 12 && clickY >= starY && clickY <= starY + 12) {
            Config config = Config.load();
            if (config.favorites.contains(this.packId)) {
                config.favorites.remove(this.packId);
                this.favorite = false;
            } else {
                config.favorites.add(this.packId);
                this.favorite = true;
            }
            config.save();
            cir.setReturnValue(true);
        }
    }
}
