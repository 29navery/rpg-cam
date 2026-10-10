package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("RETURN"))
    private void rpgcam$smoothVisibleFacing(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (entity == Minecraft.getInstance().player && RpgCamClient.active()) {
            // Render turning independently of the exact rotation used for vanilla actions and packets.
            state.bodyRot = RpgCamClient.visualYaw(partialTick);
            state.yRot = 0;
        }
    }
}
