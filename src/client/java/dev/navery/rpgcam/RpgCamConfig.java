package dev.navery.rpgcam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.LoggerFactory;

public final class RpgCamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("rpg-cam.json");
    public boolean enabled = true;
    public double smoothingMultiplier = 0.5;
    public double playerTurnSpeed = 5.0;
    public double cameraYOffset = 0.0;
    public double cameraXOffset = 0.0;

    public static RpgCamConfig load() {
        try {
            if (Files.exists(FILE)) {
                RpgCamConfig config = GSON.fromJson(Files.readString(FILE), RpgCamConfig.class);
                if (config != null) { config.validate(); return config; }
            }
        } catch (Exception e) { LoggerFactory.getLogger("rpg_cam").warn("Could not load RPG Cam settings", e); }
        return new RpgCamConfig();
    }
    public void validate() {
        smoothingMultiplier = clamp(smoothingMultiplier, 0.1, 1, 0.5);
        playerTurnSpeed = clamp(playerTurnSpeed, 0, 10, 5);
        cameraYOffset = Math.round(clamp(cameraYOffset, 0, 2, 0) * 10) / 10.0;
        cameraXOffset = Math.round(clamp(cameraXOffset, -1, 1, 0) * 10) / 10.0;
    }
    private static double clamp(double v, double min, double max, double fallback) {
        return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : fallback;
    }
    public void save() {
        validate();
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(this)); }
        catch (Exception e) { LoggerFactory.getLogger("rpg_cam").error("Could not save RPG Cam settings", e); }
    }
}
