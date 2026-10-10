package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void rpgcam$orbit(double yaw, double pitch, CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player && RpgCamClient.active()) {
            RpgCamClient.orbit(yaw, pitch);
            ci.cancel();
        }
    }
}
