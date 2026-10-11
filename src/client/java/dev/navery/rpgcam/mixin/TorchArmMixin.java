package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.RpgCamClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class TorchArmMixin {
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Unique private static final Identifier RPGCAM_TORCH_PACK =
        Identifier.fromNamespaceAndPath("rpg_cam", "held_torches.json");

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
    private void rpgcam$holdTorchForward(HumanoidRenderState state, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (!RpgCamClient.CONFIG.enabled || !(state instanceof AvatarRenderState avatar)
            || client.player == null || avatar.id != client.player.getId()
            || client.gameRenderer.mainCamera().isPanoramicMode()
            || client.getResourceManager().getResource(RPGCAM_TORCH_PACK).isEmpty()
            || state.bedOrientation != null || state.isFallFlying || state.swimAmount > 0
            || state.deathTime > 0 || state.isAutoSpinAttack || state.isUsingItem
            || state.rightArmPose.isTwoHanded() || state.leftArmPose.isTwoHanded()) return;
        rpgcam$poseArm(state, HumanoidArm.RIGHT, state.rightHandItemStack, rightArm);
        rpgcam$poseArm(state, HumanoidArm.LEFT, state.leftHandItemStack, leftArm);
    }

    @Unique private static void rpgcam$poseArm(HumanoidRenderState state, HumanoidArm side, ItemStack item, ModelPart arm) {
        if (!(item.is(Items.TORCH) || item.is(Items.SOUL_TORCH)
            || item.is(Items.REDSTONE_TORCH) || item.is(Items.COPPER_TORCH))) return;
        // Let placing/attacking finish normally, including with a torch in the offhand.
        if (state.swingAnimation > 0 && state.currentSwing != null
            && state.currentSwing.hand().asArm(state.mainArm) == side) return;
        arm.xRot = -(float)Math.PI / 2;
        arm.yRot = 0;
        arm.zRot = 0;
    }
}
