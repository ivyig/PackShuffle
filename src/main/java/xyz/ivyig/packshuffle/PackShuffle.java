package xyz.ivyig.packshuffle;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import java.util.*;

public class PackShuffle {
    private static DelegatingResourcePack slotPack;
    private static Pack slotProfile;

    public static PackResources getSlotPack() {
        return slotPack;
    }

    public static String getSlotTargetName() {
        Config config = Config.load();
        if (config.lastShuffled == null || config.lastShuffled.isEmpty()) {
            return "None";
        }
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.getResourcePackRepository() != null) {
            Pack profile = client.getResourcePackRepository().getPack(config.lastShuffled);
            if (profile != null) {
                return profile.getTitle().getString();
            }
        }
        return config.lastShuffled;
    }

    private static String cachedLastShuffled = null;

    public static Pack getOrCreateSlotProfile() {
        if (slotPack == null) {
            PackLocationInfo info = new PackLocationInfo(
                "file/packshuffle_slot",
                Component.literal("PackShuffle Slot"),
                PackSource.BUILT_IN,
                Optional.empty()
            );
            Minecraft client = Minecraft.getInstance();
            java.nio.file.Path rootPath;
            if (client != null && client.getResourcePackDirectory() != null) {
                rootPath = client.getResourcePackDirectory().resolve("packshuffle_slot");
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
        slotProfile = new Pack(
            slotPack.location(),
            new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) {
                    return slotPack;
                }
                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                    return slotPack;
                }
            },
            new Pack.Metadata(
                Component.literal("Currently: " + getSlotTargetName()),
                PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(),
                List.of()
            ),
            new PackSelectionConfig(
                false,
                Pack.Position.TOP,
                false
            )
        );
        return slotProfile;
    }

    public static void initializeSlotPack(Minecraft mc) {
        try {
            java.nio.file.Path rpDir = mc.getResourcePackDirectory();
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
        PackRepository repo = mc.getResourcePackRepository();
        if (repo == null) {
            return;
        }

        PackResources delegate = null;
        if (config.lastShuffled != null && !config.lastShuffled.isEmpty()) {
            Pack profile = repo.getPack(config.lastShuffled);
            if (profile != null) {
                delegate = profile.open();
            }
        }

        if (delegate == null) {
            shuffleNoReload(mc);
        } else {
            slotPack.setDelegate(delegate);
            ensureSlotPackEnabled(mc);
        }
    }

    private static void ensureSlotPackEnabled(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();
        if (repo == null) return;
        List<String> enabled = new ArrayList<>(repo.getSelectedIds());
        if (!enabled.contains("file/packshuffle_slot")) {
            enabled.add("file/packshuffle_slot");
            repo.setSelected(enabled);
            mc.options.updateResourcePacks(repo);
            mc.options.save();
        }
    }

    public static void shuffle(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();
        if (repo == null || !repo.getSelectedIds().contains("file/packshuffle_slot")) {
            return;
        }
        if (shuffleNoReload(mc)) {
            mc.reloadResourcePacks();
        }
    }

    private static boolean shuffleNoReload(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();
        if (repo == null) {
            return false;
        }

        getOrCreateSlotProfile();
        Config config = Config.load();
        List<String> available = new ArrayList<>(repo.getAvailableIds());

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

        Pack profile = repo.getPack(chosenId);
        if (profile == null) {
            return false;
        }

        slotPack.setDelegate(profile.open());
        config.lastShuffled = chosenId;
        config.save();

        ensureSlotPackEnabled(mc);

        return true;
    }
}
