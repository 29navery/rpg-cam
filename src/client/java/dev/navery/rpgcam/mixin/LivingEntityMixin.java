package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
    private void rpgcam$keepBodyFacing(float wantedYaw, CallbackInfo ci) {
        if ((Object)this == Minecraft.getInstance().player && RpgCamClient.active()) {
            LocalPlayer player = Minecraft.getInstance().player;
            player.yBodyRot = player.getYRot();
            player.yHeadRot = player.getYRot();
            ci.cancel();
        }
    }
    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float rpgcam$sprintJumpDirection(LivingEntity entity) {
        if (entity == Minecraft.getInstance().player && RpgCamClient.active()) {
            Vec2 input = Minecraft.getInstance().player.input.getMoveVector();
            if (input.lengthSquared() > 1.0e-6f) return RpgCamClient.movementYaw(input.x, input.y);
        }
        return entity.getYRot();
    }
    @Inject(method = "swing", at = @At("HEAD"))
    private void rpgcam$aimBeforeSwing(InteractionHand hand, SwingAnimation animation, boolean broadcast,
        CallbackInfoReturnable<Boolean> ci) {
        if ((Object)this == Minecraft.getInstance().player) RpgCamClient.faceCursor(true);
    }

}
