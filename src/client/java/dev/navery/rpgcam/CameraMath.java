package dev.navery.rpgcam;

/** Coordinate and response calculations shared by the camera and movement controller. */
public final class CameraMath {
    private CameraMath() {}
    public static boolean hasMovement(float left, float forward) {
        return left * left + forward * forward > 1.0e-6f;
    }
    public static float smoothAngle(float current, float wanted, double multiplier, double seconds) {
        float difference = (wanted - current) % 360;
        if (difference >= 180) difference -= 360;
        if (difference < -180) difference += 360;
        float result = current + difference * (float)frameResponse(multiplier, seconds);
        // Bound long-running camera orbits while keeping the shortest angular path.
        result %= 360;
        if (result >= 180) result -= 360;
        if (result < -180) result += 360;
        return result;
    }
    public static float turnTowards(float current, float wanted, double speed) {
        float difference = (wanted - current) % 360;
        if (difference >= 180) difference -= 360;
        if (difference < -180) difference += 360;
        return current + difference * (float)turnResponse(speed);
    }
    public static float[] lookAt(double x, double y, double z) {
        return new float[] {(float)Math.toDegrees(Math.atan2(-x, z)),
            (float)-Math.toDegrees(Math.atan2(y, Math.hypot(x, z)))};
    }
    public static float directionalHeadPitch(double verticalVelocity, boolean grounded) {
        // Grounded entities can retain a small downward gravity velocity: keep the head level there.
        if (grounded || !Double.isFinite(verticalVelocity) || Math.abs(verticalVelocity) < 0.01) return 0;
        // Minecraft pitch is negative when looking up. Scale with velocity and cap the visual tilt.
        return (float)Math.max(-45, Math.min(45, -verticalVelocity * 90));
    }
    public static float glideDistance(float current, float desired, float safeDistance, double seconds) {
        float target = Math.min(desired, safeDistance);
        double response = 1 - Math.exp(-8 * Math.max(0, seconds));
        // Ease changes in either direction, but never leave the camera behind an obstruction.
        return Math.max(0, Math.min(safeDistance, current + (float)((target - current) * response)));
    }
    public static boolean withinReach(double distanceSquared, double min, double max) {
        return distanceSquared >= min * min && distanceSquared <= max * max;
    }
    public static double frameResponse(double multiplier, double seconds) {
        return 1 - Math.pow(1 - multiplier, seconds * 60);
    }
    public static double turnResponse(double speed) {
        return 1 - Math.exp(-speed * 0.05);
    }
    public static float movementYaw(float cameraYaw, float left, float forward) {
        return cameraYaw - (float)Math.toDegrees(Math.atan2(left, forward));
    }
    public static float[] movementInBodySpace(float cameraYaw, float bodyYaw, float left, float forward) {
        double angle = Math.toRadians(cameraYaw - bodyYaw);
        return new float[] {(float)(left * Math.cos(angle) - forward * Math.sin(angle)),
            (float)(left * Math.sin(angle) + forward * Math.cos(angle))};
    }
}
