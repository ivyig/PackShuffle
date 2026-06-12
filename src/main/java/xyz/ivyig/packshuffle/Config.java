package xyz.ivyig.packshuffle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("packshuffle.json");

    public Set<String> favorites = new HashSet<>();
    public Set<String> locked = new HashSet<>();
    public boolean shuffleOnLaunch = true;
    public boolean shuffleOnJoin = false;
    public boolean shuffleFavoritesOnly = false;
    public String lastShuffled = "";

    private static Config instance;

    public static Config load() {
        if (instance != null) {
            return instance;
        }
        if (Files.exists(FILE_PATH)) {
            try {
                String json = Files.readString(FILE_PATH);
                Config config = GSON.fromJson(json, Config.class);
                if (config != null) {
                    if (config.favorites == null) config.favorites = new HashSet<>();
                    if (config.locked == null) config.locked = new HashSet<>();
                    instance = config;
                    return instance;
                }
            } catch (IOException e) {
            }
        }
        Config config = new Config();
        config.save();
        instance = config;
        return config;
    }

    public void save() {
        try {
            Files.writeString(FILE_PATH, GSON.toJson(this));
        } catch (IOException e) {
        }
        instance = this;
    }
}
