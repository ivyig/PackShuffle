package xyz.ivyig.packshuffle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import java.util.*;

public class PackShuffle {
    private static DelegatingResourcePack slotPack;
    private static ResourcePackProfile slotProfile;

    public static ResourcePack getSlotPack() {
        return slotPack;
    }

    public static String getSlotTargetName() {
        Config config = Config.load();
        if (config.lastShuffled == null || config.lastShuffled.isEmpty()) {
            return "None";
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getResourcePackManager() != null) {
            ResourcePackProfile profile = client.getResourcePackManager().getProfile(config.lastShuffled);
            if (profile != null) {
                return profile.getDisplayName().getString();
            }
        }
        return config.lastShuffled;
    }

    private static String cachedLastShuffled = null;

    public static ResourcePackProfile getOrCreateSlotProfile() {
        if (slotPack == null) {
            ResourcePackInfo info = new ResourcePackInfo(
                "file/packshuffle_slot",
                net.minecraft.text.Text.literal("PackShuffle Slot"),
                net.minecraft.resource.ResourcePackSource.BUILTIN,
                Optional.empty()
            );
            MinecraftClient client = MinecraftClient.getInstance();
            java.nio.file.Path rootPath;
            if (client != null && client.getResourcePackDir() != null) {
                rootPath = client.getResourcePackDir().resolve("packshuffle_slot");
            } else {
                rootPath = java.nio.file.Path.of("resourcepacks/packshuffle_slot");
            }
            slotPack = new DelegatingResourcePack(info, rootPath);
        }
        Config config = Config.load();
        String currentLastShuffled = config.lastShuffled;
        if (slotProfile != null && Objects.equals(cachedLastShuffled, currentLastShuffled)) {
            return slotProfile;
        }
        cachedLastShuffled = currentLastShuffled;
        slotProfile = new ResourcePackProfile(
            slotPack.getInfo(),
            new ResourcePackProfile.PackFactory() {
                @Override
                public ResourcePack open(ResourcePackInfo info) {
                    return slotPack;
                }
                @Override
                public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
                    return slotPack;
                }
            },
            new ResourcePackProfile.Metadata(
                net.minecraft.text.Text.literal("Currently: " + getSlotTargetName()),
                net.minecraft.resource.ResourcePackCompatibility.COMPATIBLE,
                net.minecraft.resource.featuretoggle.FeatureSet.empty(),
                java.util.List.of()
            ),
            new net.minecraft.resource.ResourcePackPosition(
                false,
                ResourcePackProfile.InsertionPosition.TOP,
                false
            )
        );
        return slotProfile;
    }

    public static void initializeSlotPack(MinecraftClient mc) {
        try {
            java.nio.file.Path rpDir = mc.getResourcePackDir();
            if (rpDir != null) {
                java.nio.file.Path slotPath = rpDir.resolve("packshuffle_slot");
                if (!java.nio.file.Files.exists(slotPath)) {
                    java.nio.file.Files.createDirectories(slotPath);
                    java.nio.file.Files.writeString(slotPath.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":34,\"description\":\"PackShuffle Slot\"}}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getOrCreateSlotProfile();
        Config config = Config.load();
        ResourcePackManager repo = mc.getResourcePackManager();
        if (repo == null) return;

        ResourcePack delegate = null;
        if (config.lastShuffled != null && !config.lastShuffled.isEmpty()) {
            ResourcePackProfile profile = repo.getProfile(config.lastShuffled);
            if (profile != null) {
                delegate = profile.createResourcePack();
            }
        }

        if (delegate == null) {
            shuffleNoReload(mc);
        } else {
            slotPack.setDelegate(delegate);
            ensureSlotPackEnabled(mc);
        }
    }

    private static void ensureSlotPackEnabled(MinecraftClient mc) {
        ResourcePackManager repo = mc.getResourcePackManager();
        if (repo == null) return;
        List<String> enabled = new ArrayList<>(repo.getEnabledIds());
        if (!enabled.contains("file/packshuffle_slot")) {
            enabled.add("file/packshuffle_slot");
            repo.setEnabledProfiles(enabled);
            mc.options.addResourcePackProfilesToManager(repo);
            mc.options.write();
        }
    }

    public static void shuffle(MinecraftClient mc) {
        ResourcePackManager repo = mc.getResourcePackManager();
        if (repo == null || !repo.getEnabledIds().contains("file/packshuffle_slot")) {
            return;
        }
        if (shuffleNoReload(mc)) {
            mc.reloadResources();
        }
    }

    private static boolean shuffleNoReload(MinecraftClient mc) {
        ResourcePackManager repo = mc.getResourcePackManager();
        if (repo == null) {
            return false;
        }

        getOrCreateSlotProfile();
        Config config = Config.load();
        List<String> available = new ArrayList<>(repo.getIds());

        List<String> candidates = new ArrayList<>();
        if (config.shuffleFavoritesOnly) {
            for (String id : available) {
                if (id.startsWith("file/") && !id.equals("file/packshuffle_slot") && config.favorites.contains(id)) {
                    candidates.add(id);
                }
            }
        } else {
            for (String id : available) {
                if (id.startsWith("file/") && !id.equals("file/packshuffle_slot")) {
                    candidates.add(id);
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        if (candidates.size() > 1 && config.lastShuffled != null && !config.lastShuffled.isEmpty()) {
            candidates.remove(config.lastShuffled);
        }

        Random rand = new Random();
        String chosenId = candidates.get(rand.nextInt(candidates.size()));

        ResourcePackProfile profile = repo.getProfile(chosenId);
        if (profile == null) {
            return false;
        }

        slotPack.setDelegate(profile.createResourcePack());
        config.lastShuffled = chosenId;
        config.save();

        ensureSlotPackEnabled(mc);

        return true;
    }
}
