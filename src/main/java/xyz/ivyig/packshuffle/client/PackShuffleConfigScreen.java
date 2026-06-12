package xyz.ivyig.packshuffle.client;

import xyz.ivyig.packshuffle.Config;
import xyz.ivyig.packshuffle.PackShuffle;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PackShuffleConfigScreen extends Screen {
    private final Screen parent;

    public PackShuffleConfigScreen(Screen parent) {
        super(Text.literal("Pack Shuffle Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Config config = Config.load();

        ButtonWidget toggleLaunch = ButtonWidget.builder(
            Text.literal("Shuffle on Launch: " + (config.shuffleOnLaunch ? "ON" : "OFF")),
            btn -> {
                config.shuffleOnLaunch = !config.shuffleOnLaunch;
                config.save();
                btn.setMessage(Text.literal("Shuffle on Launch: " + (config.shuffleOnLaunch ? "ON" : "OFF")));
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 - 40, 200, 20).build();

        ButtonWidget toggleJoin = ButtonWidget.builder(
            Text.literal("Shuffle on Server Join: " + (config.shuffleOnJoin ? "ON" : "OFF")),
            btn -> {
                config.shuffleOnJoin = !config.shuffleOnJoin;
                config.save();
                btn.setMessage(Text.literal("Shuffle on Server Join: " + (config.shuffleOnJoin ? "ON" : "OFF")));
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 - 15, 200, 20).build();

        ButtonWidget toggleMode = ButtonWidget.builder(
            Text.literal("Shuffle Pool: " + (config.shuffleFavoritesOnly ? "Favorites Only" : "All Packs")),
            btn -> {
                config.shuffleFavoritesOnly = !config.shuffleFavoritesOnly;
                config.save();
                btn.setMessage(Text.literal("Shuffle Pool: " + (config.shuffleFavoritesOnly ? "Favorites Only" : "All Packs")));
            }
        ).dimensions(this.width / 2 - 100, this.height / 2 + 10, 200, 20).build();

        ButtonWidget done = ButtonWidget.builder(
            Text.literal("Done"),
            btn -> this.client.setScreen(this.parent)
        ).dimensions(this.width / 2 - 100, this.height / 2 + 45, 200, 20).build();

        this.addDrawableChild(toggleLaunch);
        this.addDrawableChild(toggleJoin);
        this.addDrawableChild(toggleMode);
        this.addDrawableChild(done);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, net.minecraft.text.Text.literal("Active Pack: " + PackShuffle.getSlotTargetName()), this.width / 2, this.height / 2 - 55, 0xFF88FF88);
    }
}
