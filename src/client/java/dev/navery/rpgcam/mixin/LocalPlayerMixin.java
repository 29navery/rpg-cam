package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import dev.navery.rpgcam.CameraAim;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import dev.navery.rpgcam.CameraMath;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = {"aiStep", "canStartSprinting", "shouldStopRunSprinting"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"))
    private boolean rpgcam$sprintInAnyDirection(ClientInput input) {
        if (RpgCamClient.omnidirectionalSprintAllowed()) {
            var movement = input.getMoveVector();
            return CameraMath.hasMovement(movement.x, movement.y);
        }
        return input.hasForwardImpulse();
    }
    // Preserve directional double-tap sprinting instead of resetting the timer on every backward tap.
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Input;backward()Z"))
    private boolean rpgcam$backwardSprintTap(Input input) {
        return !RpgCamClient.omnidirectionalSprintAllowed() && input.backward();
    }

}
