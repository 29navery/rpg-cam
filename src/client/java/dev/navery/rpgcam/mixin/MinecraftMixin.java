package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void rpgcam$aimBeforeAttack(CallbackInfoReturnable<Boolean> ci) { RpgCamClient.faceCursor(true); }
    @Inject(method = "continueAttack", at = @At("HEAD"))
    private void rpgcam$aimWhileMining(boolean down, CallbackInfo ci) {
        if (down) RpgCamClient.faceCursor(true);
    }
    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void rpgcam$aimBeforeInteraction(CallbackInfo ci) { RpgCamClient.faceCursor(true); }
}
