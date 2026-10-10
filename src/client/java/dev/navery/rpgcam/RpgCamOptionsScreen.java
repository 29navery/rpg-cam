package dev.navery.rpgcam;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.Consumer;

public final class RpgCamOptionsScreen extends OptionsSubScreen {
    public RpgCamOptionsScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("RPG Cam Settings"));
    }
    @Override
    protected void addOptions() {
        RpgCamConfig c = RpgCamClient.CONFIG;
        if (list == null) return;
        list.addBig(toggle("rpg_cam.enabled", c.enabled, value -> {
            c.enabled = value; c.save();
        }));
        list.addSmall(
            slider("rpg_cam.smoothing", 1, 10, (int)Math.round(c.smoothingMultiplier * 10), value -> c.smoothingMultiplier = value / 10.0),
            slider("rpg_cam.turn_speed", 0, 100, (int)Math.round(c.playerTurnSpeed * 10), value -> c.playerTurnSpeed = value / 10.0));
        list.addSmall(
            slider("rpg_cam.x_offset", -10, 10, (int)Math.round(c.cameraXOffset * 10), value -> c.cameraXOffset = value / 10.0),
            slider("rpg_cam.y_offset", 0, 20, (int)Math.round(c.cameraYOffset * 10), value -> c.cameraYOffset = value / 10.0));
        list.addBig(OptionInstance.createBoolean("rpg_cam.head_rotation", OptionInstance.noTooltip(),
            (caption, directional) -> Component.translatable(directional ? "rpg_cam.head_directional" : "rpg_cam.head_camera"),
            c.directionalHeadPitch, value -> { c.directionalHeadPitch = value; c.save(); }));
        list.addBig(toggle("rpg_cam.dither", c.dither, value -> { c.dither = value; c.save(); }));
    }
    private OptionInstance<Boolean> toggle(String key, boolean initial, Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(key, OptionInstance.noTooltip(),
            (caption, value) -> (value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
                .copy().withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED),
            initial, setter::accept);
    }
    private OptionInstance<Integer> slider(String key, int min, int max, int initial, IntConsumer setter) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
            (caption, value) -> Component.literal(caption.getString() + ": " + String.format(Locale.ROOT, "%.1f", value / 10.0)),
            new OptionInstance.IntRange(min, max), initial, value -> { setter.accept(value); RpgCamClient.CONFIG.save(); });
    }
    @Override public void removed() { RpgCamClient.CONFIG.save(); }
}
