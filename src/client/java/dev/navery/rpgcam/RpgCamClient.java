package dev.navery.rpgcam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class RpgCamClient implements ClientModInitializer {
    public static final RpgCamConfig CONFIG = RpgCamConfig.load();
    private static LocalPlayer trackedPlayer;
    private static boolean initialized;
    private static float targetYaw, targetPitch, yaw, pitch, visualYaw, visualYawOld;
    private static float visualHeadYawOffset, visualHeadYawVelocity, visualHeadPitch;
    private static KeyMapping openSettings;
    private static long lastFrame, lastDistanceFrame;
    private static boolean distanceInitialized;
    private static float distance;
    private static int aimUntilTick;
    private static int lastAimSendTick = -1;
    private static float lastAimSendYaw, lastAimSendPitch;

    @Override public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("rpg_cam", "settings"));
        openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.rpg_cam.open_settings",
            InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!active()) reset();
            while (openSettings.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) client.gui.setScreen(new RpgCamOptionsScreen(null));
            }
        });
    }
    public static boolean sleepingView() {
        Minecraft mc = Minecraft.getInstance();
        return CONFIG.enabled && mc.player != null && mc.getCameraEntity() == mc.player && mc.player.isSleeping();
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
            distanceInitialized = false;
            aimUntilTick = 0;
            lastAimSendTick = -1;
            targetYaw = yaw = player.getYRot();
            visualYaw = visualYawOld = player.yBodyRot;
            targetPitch = pitch = player.getXRot();
            visualHeadYawOffset = Mth.clamp(Mth.wrapDegrees(player.yHeadRot - player.yBodyRot), -75, 75);
            visualHeadYawVelocity = 0;
            visualHeadPitch = player.getXRot();
            lastFrame = System.nanoTime();
            initialized = true;
        }
    }
    public static void reset() {
        distanceInitialized = false;
        initialized = false; trackedPlayer = null; aimUntilTick = 0; lastAimSendTick = -1;
    }
    public static void orbit(double dx, double dy) {
        initialize();
        targetYaw = yaw + Mth.wrapDegrees((float)(targetYaw - yaw + dx * 0.15));
        targetPitch = Mth.clamp((float)(targetPitch + dy * 0.15), -90, 90);
        LocalPlayer player = Minecraft.getInstance().player;
        float previous = player.getXRot();
        player.setXRot(targetPitch);
        player.xRotO += player.getXRot() - previous;
    }
    public static void updateCamera(float partialTick) {
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
        // Smooth visible head motion once per camera frame, independently of exact action rotation.
        float headYawTarget = CONFIG.directionalHeadPitch ? 0
            : CameraMath.headYawTarget(yaw - visualYaw(partialTick), CONFIG.realisticHeadMovement);
        float headPitchTarget = CONFIG.directionalHeadPitch
            ? CameraMath.directionalHeadPitch(trackedPlayer.getDeltaMovement().y, trackedPlayer.onGround()) : pitch;
        CameraMath.HeadMotion head = CameraMath.springHeadYaw(visualHeadYawOffset, visualHeadYawVelocity,
            headYawTarget, CONFIG.smoothingMultiplier, elapsed);
        visualHeadYawOffset = head.yaw();
        visualHeadYawVelocity = head.velocity();
        visualHeadPitch += (float)((headPitchTarget - visualHeadPitch) * alpha);
    }
    public static float distanceTraceLength() {
        initialize();
        return (float)Math.max(CONFIG.cameraDistance, distanceInitialized ? distance : 0);
    }
    public static float updateDistance(float safeDistance) {
        initialize();
        long now = System.nanoTime();
        if (!distanceInitialized) {
            distance = (float)Math.min(CONFIG.cameraDistance, safeDistance);
            distanceInitialized = true;
        } else {
            double elapsed = Math.min((now - lastDistanceFrame) / 1_000_000_000.0, 0.1);
            distance = CameraMath.glideDistance(distance, (float)CONFIG.cameraDistance, safeDistance, elapsed);
        }
        lastDistanceFrame = now;
        return distance;
    }
    public static float yaw() { initialize(); return yaw; }
    public static float pitch() { initialize(); return pitch; }
    public static float movementYaw(float left, float forward) {
        return CameraMath.movementYaw(yaw(), left, forward);
    }

    public static boolean aiming() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && (player.tickCount < aimUntilTick || player.isUsingItem() || player.isSwinging()
            || Minecraft.getInstance().gameMode.isDestroying());
    }
    public static boolean omnidirectionalSprintAllowed() {
        return active() && !aiming() && !Minecraft.getInstance().player.isSwimming();
    }
    public static float visualYaw(float partialTick) {
        initialize();
        return Mth.rotLerp(partialTick, visualYawOld, visualYaw);
    }
    public static float visualHeadYawOffset() { initialize(); return visualHeadYawOffset; }
    public static float visualHeadPitch() { initialize(); return visualHeadPitch; }
    public static void keepBodyFacing(LocalPlayer player) {
        player.yHeadRot = player.getYRot();
        player.yBodyRot = player.getYRot();
    }
    public static void applyMovement(LocalPlayer player) {
        initialize();
        float left = player.xxa, forward = player.zza;
        boolean aiming = aiming();
        boolean moving = CameraMath.hasMovement(left, forward);
        float wanted;
        if (aiming) {
            alignWithCursor(player);
            wanted = player.getYRot();
        } else {
            player.setXRot(targetPitch);
            wanted = moving ? movementYaw(left, forward) : player.getYRot();
        }
        // Advance the visible body/head exactly once per simulation tick. Action hooks only set the aim.
        visualYawOld = visualYaw;
        visualYaw = CameraMath.turnTowards(visualYaw, wanted, CONFIG.playerTurnSpeed);
        if (!aiming && moving) player.setYRot(visualYaw);
        keepBodyFacing(player);
        if (!moving) return;
        // Compensate for body turn lag so movement always uses the camera's basis.
        float[] movement = CameraMath.movementInBodySpace(yaw(), player.getYRot(), left, forward);
        player.xxa = movement[0];
        player.zza = movement[1];
    }
    private static void alignWithCursor(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        // Refresh targeting too, so the action and the character's facing share one aim point.
        mc.hitResult = CameraAim.pick(player, 1.0f);
        mc.crosshairPickEntity = mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit ? hit.getEntity() : null;
        net.minecraft.world.phys.Vec3 direction = CameraAim.aimPoint().subtract(player.getEyePosition());
        if (direction.lengthSqr() < 1.0e-8) return;
        float[] look = CameraMath.lookAt(direction.x, direction.y, direction.z);
        player.setYRot(look[0]);
        player.setXRot(look[1]);
        keepBodyFacing(player);
    }
    public static void faceCursorIfTarget() {
        if (!active()) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        // CameraAim applies player reach and visibility, so distant/obstructed objects count as misses.
        mc.hitResult = CameraAim.pick(player, 1.0f);
        mc.crosshairPickEntity = mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit ? hit.getEntity() : null;
        if (mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            faceCursor(true);
        }
    }
    public static void prepareItemAim() {
        if (active()) { initialize(); alignWithCursor(Minecraft.getInstance().player); }
    }
    public static void faceCursor(boolean sendRotation) {
        if (!active()) return;
        initialize();
        LocalPlayer player = Minecraft.getInstance().player;
        aimUntilTick = player.tickCount + 6;
        alignWithCursor(player);
        if (sendRotation && (lastAimSendTick != player.tickCount
            || lastAimSendYaw != player.getYRot() || lastAimSendPitch != player.getXRot())) {
            // A vanilla rotation packet precedes the vanilla action packet; no server mod is involved.
            player.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(
                player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
            lastAimSendTick = player.tickCount;
            lastAimSendYaw = player.getYRot();
            lastAimSendPitch = player.getXRot();
        }
    }

}
