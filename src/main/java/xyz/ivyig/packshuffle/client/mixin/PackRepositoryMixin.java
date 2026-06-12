package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.LinkedHashMap;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
    private void onDiscoverAvailable(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Map<String, Pack> original = cir.getReturnValue();
        Map<String, Pack> modified = new LinkedHashMap<>(original);
        Pack slotProfile = PackShuffle.getOrCreateSlotProfile();
        if (slotProfile != null) {
            modified.put(slotProfile.getId(), slotProfile);
        }
        cir.setReturnValue(modified);
    }
}
