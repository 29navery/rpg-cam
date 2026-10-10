package dev.navery.rpgcam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.IntConsumer;

public final class RpgCamOptionsScreen extends OptionsSubScreen {
    public RpgCamOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("RPG Cam Settings"));
    }
    @Override
    protected void addOptions() {
        RpgCamConfig c = RpgCamClient.CONFIG;
        if (list == null) return;
        list.addBig(OptionInstance.createBoolean("rpg_cam.enabled", c.enabled, value -> {
            c.enabled = value; c.save();
        }));
        list.addSmall(
            slider("rpg_cam.smoothing", 1, 10, (int)Math.round(c.smoothingMultiplier * 10), value -> c.smoothingMultiplier = value / 10.0),
            slider("rpg_cam.turn_speed", 0, 100, (int)Math.round(c.playerTurnSpeed * 10), value -> c.playerTurnSpeed = value / 10.0));
        list.addSmall(
            slider("rpg_cam.x_offset", -10, 10, (int)Math.round(c.cameraXOffset * 10), value -> c.cameraXOffset = value / 10.0),
            slider("rpg_cam.y_offset", 0, 20, (int)Math.round(c.cameraYOffset * 10), value -> c.cameraYOffset = value / 10.0));
    }
    private OptionInstance<Integer> slider(String key, int min, int max, int initial, IntConsumer setter) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
            (caption, value) -> Component.literal(caption.getString() + ": " + String.format(Locale.ROOT, "%.1f", value / 10.0)),
            new OptionInstance.IntRange(min, max), initial, value -> { setter.accept(value); RpgCamClient.CONFIG.save(); });
    }
    @Override public void removed() { RpgCamClient.CONFIG.save(); }
}
