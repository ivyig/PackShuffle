package xyz.ivyig.packshuffle.client;

import xyz.ivyig.packshuffle.Config;
import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PackShuffleConfigScreen extends Screen {
    private final Screen parent;

    public PackShuffleConfigScreen(Screen parent) {
        super(Component.literal("Pack Shuffle Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Config config = Config.load();

        Button toggleLaunch = Button.builder(
            Component.literal("Shuffle on Launch: " + (config.shuffleOnLaunch ? "ON" : "OFF")),
            btn -> {
                config.shuffleOnLaunch = !config.shuffleOnLaunch;
                config.save();
                btn.setMessage(Component.literal("Shuffle on Launch: " + (config.shuffleOnLaunch ? "ON" : "OFF")));
            }
        ).bounds(this.width / 2 - 100, this.height / 2 - 40, 200, 20).build();

        Button toggleJoin = Button.builder(
            Component.literal("Shuffle on Server Join: " + (config.shuffleOnJoin ? "ON" : "OFF")),
            btn -> {
                config.shuffleOnJoin = !config.shuffleOnJoin;
                config.save();
                btn.setMessage(Component.literal("Shuffle on Server Join: " + (config.shuffleOnJoin ? "ON" : "OFF")));
            }
        ).bounds(this.width / 2 - 100, this.height / 2 - 15, 200, 20).build();

        Button toggleMode = Button.builder(
            Component.literal("Shuffle Pool: " + (config.shuffleFavoritesOnly ? "Favorites Only" : "All Packs")),
            btn -> {
                config.shuffleFavoritesOnly = !config.shuffleFavoritesOnly;
                config.save();
                btn.setMessage(Component.literal("Shuffle Pool: " + (config.shuffleFavoritesOnly ? "Favorites Only" : "All Packs")));
            }
        ).bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20).build();

        Button done = Button.builder(
            Component.literal("Done"),
            btn -> this.minecraft.setScreen(this.parent)
        ).bounds(this.width / 2 - 100, this.height / 2 + 45, 200, 20).build();

        this.addRenderableWidget(toggleLaunch);
        this.addRenderableWidget(toggleJoin);
        this.addRenderableWidget(toggleMode);
        this.addRenderableWidget(done);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractRenderState(graphics, mouseX, mouseY, tickDelta);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal("Active Pack: " + PackShuffle.getSlotTargetName()), this.width / 2, this.height / 2 - 55, 0xFF88FF88);
    }
}
