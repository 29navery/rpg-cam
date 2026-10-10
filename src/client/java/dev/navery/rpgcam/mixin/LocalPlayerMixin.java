package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import dev.navery.rpgcam.CameraAim;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "applyInput", at = @At("RETURN"))
    private void rpgcam$movement(CallbackInfo ci) {
        if (RpgCamClient.active()) RpgCamClient.applyMovement((LocalPlayer)(Object)this);
    }
    @Inject(method = "raycastHitResult", at = @At("HEAD"), cancellable = true)
    private void rpgcam$cameraTarget(float partialTicks, Entity cameraEntity, CallbackInfoReturnable<HitResult> ci) {
        if (RpgCamClient.active()) ci.setReturnValue(CameraAim.pick((LocalPlayer)(Object)this, partialTicks));
    }

}
