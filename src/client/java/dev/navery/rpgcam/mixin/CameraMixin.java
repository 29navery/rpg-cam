package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.RpgCamClient;
import dev.navery.rpgcam.CameraMath;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Level level;
    @Shadow private Entity entity;
    @Shadow private Vec3 position;
    @Shadow private boolean detached;
    @Unique private Entity rpgcam$sleepingEntity;
    @Unique private float rpgcam$sleepDistance;
    @Unique private long rpgcam$sleepFrame;

    @Inject(method = "alignWithEntity", at = @At("HEAD"), cancellable = true)
    private void rpgcam$sleepingView(float partialTick, CallbackInfo ci) {
        if (!RpgCamClient.sleepingView() || entity != Minecraft.getInstance().player) { rpgcam$sleepingEntity = null; return; }
        var player = Minecraft.getInstance().player;
        Direction bedDirection = player.getBedOrientation();
        // Center over the whole bed/body instead of orbiting the player's pillow position.
        Vec3 center = new Vec3(Mth.lerp(partialTick, player.xo, player.getX()),
            Mth.lerp(partialTick, player.yo, player.getY()) + 0.15,
            Mth.lerp(partialTick, player.zo, player.getZ()));
        if (bedDirection != null) center = center.add(-bedDirection.getStepX() * 0.5, 0, -bedDirection.getStepZ() * 0.5);
        setRotation(bedDirection != null ? bedDirection.toYRot() : player.getYRot(), 90);
        setPosition(center);
        detached = true; // Render the whole local player even when their selected view is first person.
        // Raycast the entire five-block destination before animating. The reachable distance is the target.
        float target = CameraMath.sleepDistanceTarget(getMaxZoom(5));
        long now = System.nanoTime();
        if (rpgcam$sleepingEntity != player) {
            rpgcam$sleepingEntity = player;
            rpgcam$sleepDistance = Math.min(0.15f, target);
        } else {
            double elapsed = Math.min((now - rpgcam$sleepFrame) / 1_000_000_000.0, 0.1);
            rpgcam$sleepDistance = CameraMath.glideDistance(rpgcam$sleepDistance, target, target, elapsed);
            if (Math.abs(rpgcam$sleepDistance - target) < 0.005) rpgcam$sleepDistance = target;
        }
        rpgcam$sleepFrame = now;
        setPosition(center.add(0, rpgcam$sleepDistance, 0));
        ci.cancel();
    }

    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 position);

    @Shadow private float getMaxZoom(float distance) { throw new AssertionError(); }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private float rpgcam$glideDistance(Camera camera, float vanillaDistance) {
        if (!RpgCamClient.active()) return getMaxZoom(vanillaDistance);
        // Trace far enough to cover both current and requested distances, including slider reductions.
        float safeDistance = getMaxZoom(RpgCamClient.distanceTraceLength());
        return RpgCamClient.updateDistance(safeDistance);
    }

    // Run before vanilla obstruction testing, so its zoom rays use our orbit direction.
    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void rpgcam$camera(float partialTick, CallbackInfo ci) {
        if (!RpgCamClient.active()) return;
        RpgCamClient.updateCamera(partialTick);
        setRotation(RpgCamClient.yaw(), RpgCamClient.pitch());
        double yaw = Math.toRadians(RpgCamClient.yaw());
        Vec3 offset = new Vec3(-Math.cos(yaw) * RpgCamClient.CONFIG.cameraXOffset,
            RpgCamClient.CONFIG.cameraYOffset, -Math.sin(yaw) * RpgCamClient.CONFIG.cameraXOffset);
        Vec3 end = position.add(offset);
        HitResult hit = level.clip(new ClipContext(position, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        if (hit.getType() != HitResult.Type.MISS && offset.lengthSqr() > 1.0e-6) {
            double distance = Math.max(0, position.distanceTo(hit.getLocation()) - 0.15);
            end = position.add(offset.normalize().scale(distance));
        }
        setPosition(end);
    }
}
