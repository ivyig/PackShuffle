package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.Config;
import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin {
    @Inject(method = "startConnecting", at = @At("HEAD"))
    private static void onStartConnecting(Screen parent, Minecraft minecraft, ServerAddress address, ServerData serverData, boolean quickPlay, TransferState transferState, CallbackInfo ci) {
        Config config = Config.load();
        if (config.shuffleOnJoin) {
            PackShuffle.shuffle(minecraft);
        }
    }
}
