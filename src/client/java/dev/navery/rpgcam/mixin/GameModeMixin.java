package dev.navery.rpgcam.mixin;
import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class GameModeMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void rpgcam$aimBeforeRelease(Player player, CallbackInfo ci) {
        if (player == Minecraft.getInstance().player) RpgCamClient.faceCursor(true);
    }
}
