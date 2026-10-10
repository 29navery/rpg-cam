package dev.navery.rpgcam.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderPipelines.class)
public abstract class RenderPipelinesMixin {
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target =
        "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;withFragmentShader(Ljava/lang/String;)Lcom/mojang/renderpearl/api/pipeline/RenderPipeline$Builder;"))
    private static RenderPipeline.Builder rpgcam$terrainShader(RenderPipeline.Builder builder, String shader) {
        // Covers both ordinary and multi-draw terrain, including cutout and OIT variants.
        return builder.withFragmentShader("core/terrain".equals(shader) ? "rpg_cam:core/terrain" : shader);
    }
}
