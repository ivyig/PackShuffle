package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.packs.TransferableSelectionList$PackEntry")
public abstract class PackEntryMixin {
    @Shadow @Final protected Minecraft minecraft;
    @Shadow @Final private PackSelectionModel.Entry pack;

    @Shadow @Final private net.minecraft.client.gui.components.StringWidget nameWidget;
    @Shadow @Final private net.minecraft.client.gui.components.MultiLineTextWidget descriptionWidget;

    private boolean normalPack;
    private boolean isShuffleSlot;
    private String packId;
    private boolean favorite;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(TransferableSelectionList parentList, Minecraft minecraft, TransferableSelectionList list, PackSelectionModel.Entry packEntry, CallbackInfo ci) {
        if (packEntry != null) {
            this.packId = packEntry.getId();
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
        method = "extractContent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/StringWidget;setMaxWidth(I)Lnet/minecraft/client/gui/components/StringWidget;"
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
        method = "extractContent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/MultiLineTextWidget;setMaxWidth(I)Lnet/minecraft/client/gui/components/MultiLineTextWidget;"
        ),
        index = 0
    )
    private int modifyDescriptionMaxWidth(int maxWidth) {
        if (!this.normalPack || this.isShuffleSlot) {
            return maxWidth;
        }
        return maxWidth - 20;
    }

    @Inject(method = "extractContent", at = @At("TAIL"))
    private void onExtractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (!this.normalPack || this.isShuffleSlot) {
            return;
        }
        TransferableSelectionList.Entry entry = (TransferableSelectionList.Entry) (Object) this;
        int right = entry.getContentRight();
        int top = entry.getContentY();

        int starX = right - 16;
        int starY = top + 10;
        boolean starHovered = mouseX >= starX && mouseX <= starX + 12 && mouseY >= starY && mouseY <= starY + 12;

        String starChar = this.favorite ? "★" : (hovered ? "☆" : "");
        int starColor = this.favorite ? 0xFFFFD700 : (starHovered ? 0xFFFFFFFF : 0xFF888888);

        graphics.text(minecraft.font, starChar, starX + 2, starY + 1, starColor);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!this.normalPack || this.isShuffleSlot) {
            return;
        }
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        TransferableSelectionList.Entry entry = (TransferableSelectionList.Entry) (Object) this;
        int right = entry.getContentRight();
        int top = entry.getContentY();

        int starX = right - 16;
        int starY = top + 10;

        if (mouseX >= starX && mouseX <= starX + 12 && mouseY >= starY && mouseY <= starY + 12) {
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
