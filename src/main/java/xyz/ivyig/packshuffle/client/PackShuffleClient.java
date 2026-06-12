package xyz.ivyig.packshuffle.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import xyz.ivyig.packshuffle.PackShuffle;
import xyz.ivyig.packshuffle.Config;

public class PackShuffleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {


            PackShuffle.initializeSlotPack(client);
            Config config = Config.load();
            if (config.shuffleOnLaunch) {
                PackShuffle.shuffle(client);
            }
        });
    }
}
