package xyz.ivyig.packshuffle.client.mixin;

import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.LinkedHashMap;

@Mixin(ResourcePackManager.class)
public class ResourcePackManagerMixin {
    @Inject(method = "providePackProfiles", at = @At("RETURN"), cancellable = true)
    private void onProvidePackProfiles(CallbackInfoReturnable<Map<String, ResourcePackProfile>> cir) {
        Map<String, ResourcePackProfile> original = cir.getReturnValue();
        Map<String, ResourcePackProfile> modified = new LinkedHashMap<>(original);
        ResourcePackProfile slotProfile = PackShuffle.getOrCreateSlotProfile();
        if (slotProfile != null) {
            modified.put(slotProfile.getId(), slotProfile);
        }
        cir.setReturnValue(modified);
    }
}
