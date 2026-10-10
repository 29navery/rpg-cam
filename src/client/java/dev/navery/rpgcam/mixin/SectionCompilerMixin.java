package dev.navery.rpgcam.mixin;

import dev.navery.rpgcam.CameraDither;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Redirect(method = "compile", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V"))
    private void rpgcam$markNonSolid(ModelBlockRenderer renderer, BlockQuadOutput output, float x, float y, float z,
        BlockAndTintGetter level, BlockPos pos, BlockState state, BlockStateModel model, long seed) {
        BlockQuadOutput marked = output;
        if (CameraDither.markingEnabled() && CameraDither.eligible(state)) {
            marked = (qx, qy, qz, quad, instance) -> {
                int c0 = instance.getColor(0), c1 = instance.getColor(1), c2 = instance.getColor(2), c3 = instance.getColor(3);
                // Reserve alpha 254 as a per-quad marker, leaving RGB and custom translucent tint intact.
                boolean opaqueTint = (c0 >>> 24) == 255 && (c1 >>> 24) == 255 && (c2 >>> 24) == 255 && (c3 >>> 24) == 255;
                if (opaqueTint) {
                    for (int i = 0; i < 4; i++) instance.setColor(i, (instance.getColor(i) & 0x00ffffff) | 0xfe000000);
                }
                try { output.put(qx, qy, qz, quad, instance); }
                finally {
                    instance.setColor(0, c0); instance.setColor(1, c1); instance.setColor(2, c2); instance.setColor(3, c3);
                }
            };
        }
        renderer.tesselateBlock(marked, x, y, z, level, pos, state, model, seed);
    }
}
