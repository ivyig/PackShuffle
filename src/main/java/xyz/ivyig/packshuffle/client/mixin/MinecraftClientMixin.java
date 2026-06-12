package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.Config;
import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "startIntegratedServer", at = @At("HEAD"))
    private void onStartIntegratedServer(LevelStorage.Session session, ResourcePackManager resourcePackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
        Config config = Config.load();
        if (config.shuffleOnJoin) {
            PackShuffle.shuffle((MinecraftClient) (Object) this);
        }
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            screen.setFocused(null);
        }
    }
}
