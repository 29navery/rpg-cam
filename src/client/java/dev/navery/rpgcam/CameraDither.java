package dev.navery.rpgcam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Client camera obstruction policy and terrain compilation state. Never used for interaction rays. */
public final class CameraDither {
    private static volatile boolean markingEnabled;
    private static ClientLevel trackedLevel;
    private CameraDither() {}

    public static boolean eligible(BlockState state) {
        // Full opaque terrain and animated block entities retain normal camera collision/rendering.
        return !state.isAir() && !state.isSolidRender() && !state.hasBlockEntity();
    }
    public static boolean markingEnabled() { return markingEnabled; }
    public static void update(Minecraft client) {
        boolean enabled = RpgCamClient.CONFIG.dither && RpgCamClient.active();
        boolean changed = enabled != markingEnabled || trackedLevel != client.level;
        markingEnabled = enabled;
        trackedLevel = client.level;
        // Rebuild only on a view/setting/world transition, never as the camera moves.
        // Distance fading itself happens in the fragment shader on every frame.
        if (changed && client.level != null) client.levelExtractor.allChanged();
    }
    public static ClipContext cameraRay(Vec3 from, Vec3 to, Entity entity) {
        if (!RpgCamClient.CONFIG.dither || !RpgCamClient.active()) {
            return new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity);
        }
        return new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity) {
            @Override public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
                return eligible(state) ? Shapes.empty() : super.getBlockShape(state, level, pos);
            }
        };
    }
}
