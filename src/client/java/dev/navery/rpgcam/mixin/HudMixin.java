package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean rpgcam$thirdPersonCrosshair(CameraType type) {
        return type.isFirstPerson() || RpgCamClient.CONFIG.enabled && type == CameraType.THIRD_PERSON_BACK;
    }
}
