package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class GameModeMixin {
    @Unique private boolean rpgcam$preparedItemAim;
    @Unique private float rpgcam$previousYaw, rpgcam$previousPitch;

    @Inject(method = "useItem", at = @At("HEAD"))
    private void rpgcam$prepareItemAim(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        rpgcam$preparedItemAim = player == Minecraft.getInstance().player && RpgCamClient.active();
        if (!rpgcam$preparedItemAim) return;
        rpgcam$previousYaw = player.getYRot();
        rpgcam$previousPitch = player.getXRot();
        // Item/projectile prediction and the vanilla use packet need the exact cursor direction.
        // Do not start a visible turn until the item reports a successful use.
        RpgCamClient.prepareItemAim();
    }
    @Inject(method = "useItem", at = @At("RETURN"))
    private void rpgcam$finishItemAim(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if (!rpgcam$preparedItemAim) return;
        rpgcam$preparedItemAim = false;
        if (ci.getReturnValue() instanceof InteractionResult.Success) {
            RpgCamClient.faceCursor(true);
        } else {
            player.setYRot(rpgcam$previousYaw);
            player.setXRot(rpgcam$previousPitch);
            RpgCamClient.keepBodyFacing(Minecraft.getInstance().player);
        }
    }

    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void rpgcam$aimBeforeRelease(Player player, CallbackInfo ci) {
        if (player == Minecraft.getInstance().player) RpgCamClient.faceCursor(true);
    }
}
