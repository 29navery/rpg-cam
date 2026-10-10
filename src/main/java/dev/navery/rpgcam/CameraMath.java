package dev.navery.rpgcam;

/** Coordinate and response calculations shared by the camera and movement controller. */
public final class CameraMath {
    private CameraMath() {}
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
