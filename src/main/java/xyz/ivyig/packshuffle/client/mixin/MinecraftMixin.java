package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.Config;
import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void onDoWorldLoad(LevelStorageSource.LevelStorageAccess access, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, boolean bl, CallbackInfo ci) {
        Config config = Config.load();
        if (config.shuffleOnJoin) {
            PackShuffle.shuffle((Minecraft) (Object) this);
        }
    }

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            screen.clearFocus();
        }
    }
}
