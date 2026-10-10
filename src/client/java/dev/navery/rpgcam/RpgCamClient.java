package dev.navery.rpgcam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class RpgCamClient implements ClientModInitializer {
    public static final RpgCamConfig CONFIG = RpgCamConfig.load();
    private static LocalPlayer trackedPlayer;
    private static boolean initialized;
    private static float targetYaw, targetPitch, yaw, pitch;
    private static long lastFrame;

    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active()) reset();
        });
    }
    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return CONFIG.enabled && mc.player != null && mc.getCameraEntity() == mc.player
            && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK
            && !mc.player.isSleeping() && !mc.player.isPassenger() && !mc.player.isFallFlying();
    }
    private static void initialize() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!initialized || trackedPlayer != player) {
            trackedPlayer = player;
            targetYaw = yaw = player.getYRot();
            targetPitch = pitch = player.getXRot();
            lastFrame = System.nanoTime();
            initialized = true;
        }
    }
    public static void reset() { initialized = false; trackedPlayer = null; }
    public static void orbit(double dx, double dy) {
        initialize();
        targetYaw = yaw + Mth.wrapDegrees((float)(targetYaw - yaw + dx * 0.15));
        targetPitch = Mth.clamp((float)(targetPitch + dy * 0.15), -90, 90);
    }
    public static void updateCamera() {
        initialize();
        long now = System.nanoTime();
        double elapsed = Math.min((now - lastFrame) / 1_000_000_000.0, 0.1);
        lastFrame = now;
        // The multiplier is the fraction caught up per 60 Hz frame, independent of FPS.
        double alpha = CameraMath.frameResponse(CONFIG.smoothingMultiplier, elapsed);
        yaw += (float)(Mth.wrapDegrees(targetYaw - yaw) * alpha);
        pitch += (float)((targetPitch - pitch) * alpha);
        targetYaw = yaw + Mth.wrapDegrees(targetYaw - yaw);
        // Keep angles bounded without crossing the long way around at +/-180.
        float wrapped = Mth.wrapDegrees(yaw);
        targetYaw += wrapped - yaw;
        yaw = wrapped;
    }
    public static float yaw() { initialize(); return yaw; }
    public static float pitch() { initialize(); return pitch; }
    public static float movementYaw(float left, float forward) {
        return CameraMath.movementYaw(yaw(), left, forward);
    }
    public static void applyMovement(LocalPlayer player) {
        float left = player.xxa, forward = player.zza;
        if (left * left + forward * forward < 1.0e-6f) return;
        float wanted = movementYaw(left, forward);
        float amount = (float)(CameraMath.turnResponse(CONFIG.playerTurnSpeed));
        player.setYRot(player.getYRot() + Mth.wrapDegrees(wanted - player.getYRot()) * amount);
        player.setXRot(0);
        player.yHeadRot = player.getYRot();
        player.yBodyRot = player.getYRot();
        // Compensate for body turn lag so movement always uses the camera's basis.
        float[] movement = CameraMath.movementInBodySpace(yaw(), player.getYRot(), left, forward);
        player.xxa = movement[0];
        player.zza = movement[1];
    }
}
