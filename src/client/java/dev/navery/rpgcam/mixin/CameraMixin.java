package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.RpgCamClient;
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
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(Vec3 position);

    // Run before vanilla obstruction testing, so its zoom rays use our orbit direction.
    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void rpgcam$camera(float partialTick, CallbackInfo ci) {
        if (!RpgCamClient.active()) return;
        RpgCamClient.updateCamera();
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
