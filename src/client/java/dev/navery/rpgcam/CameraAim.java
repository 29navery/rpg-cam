package dev.navery.rpgcam;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Picks along the rendered camera's center ray, while retaining player reach and visibility checks. */
public final class CameraAim {
    private static Vec3 aimPoint = Vec3.ZERO;
    private CameraAim() {}
    public static Vec3 aimPoint() { return aimPoint; }

    public static HitResult pick(LocalPlayer player, float partialTick) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 from = camera.isInitialized() ? camera.position() : eye;
        Vec3 direction = camera.isInitialized() ? new Vec3(camera.forwardVector())
            : Entity.calculateViewVector(RpgCamClient.pitch(), RpgCamClient.yaw());
        AttackRange attackRange = player.getActiveItem().get(DataComponents.ATTACK_RANGE);
        double blockReach = player.blockInteractionRange();
        double entityReach = player.entityInteractionRange();
        if (attackRange != null) entityReach = Math.max(entityReach, attackRange.effectiveMaxRange(player) + attackRange.hitboxMargin());
        // A far aim point keeps thrown/fired items pointing at the crosshair even when nothing is in reach.
        double rayLength = Math.max(64, Math.max(blockReach, entityReach) + from.distanceTo(eye));
        Vec3 end = from.add(direction.scale(rayLength));
        BlockHitResult blockHit = player.level().clip(new ClipContext(from, end,
            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double blockDistance = from.distanceToSqr(blockHit.getLocation());
        AABB search = new AABB(from, end).inflate(1);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, from, end, search,
            EntitySelector.CAN_BE_PICKED, blockDistance);
        HitResult target = entityHit != null && from.distanceToSqr(entityHit.getLocation()) < blockDistance
            ? entityHit : blockHit;
        aimPoint = target.getLocation();
        if (target.getType() == HitResult.Type.MISS) return miss(aimPoint, direction);

        double distance = eye.distanceToSqr(aimPoint);
        boolean inReach = target.getType() == HitResult.Type.BLOCK
            ? CameraMath.withinReach(distance, 0, blockReach)
            : CameraMath.withinReach(distance, 0, player.entityInteractionRange())
                || attackRange != null && attackRange.isInRange(player, aimPoint);
        if (!inReach) return miss(aimPoint, direction);

        // The offset camera can see around corners. The player must also have a clear path to the target.
        BlockHitResult obstruction = player.level().clip(new ClipContext(eye, aimPoint,
            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (obstruction.getType() != HitResult.Type.MISS && eye.distanceToSqr(obstruction.getLocation()) + 1.0e-6 < distance
            && !(target instanceof BlockHitResult block && block.getBlockPos().equals(obstruction.getBlockPos()))) {
            return miss(aimPoint, direction);
        }
        return target;
    }
    private static BlockHitResult miss(Vec3 point, Vec3 direction) {
        return BlockHitResult.miss(point, Direction.getApproximateNearest(direction), BlockPos.containing(point));
    }
}
