package dev.navery.rpgcam;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CameraMathTest {
    @Test void movementFollowsCameraRegardlessOfBodyFacing() {
        float[][] inputs = {{0,1}, {0,-1}, {1,0}, {-1,0}, {.70710677f,.70710677f}};
        for (int camera = -180; camera <= 180; camera += 30) {
            for (int body = -180; body <= 180; body += 30) {
                for (float[] input : inputs) {
                    float[] local = CameraMath.movementInBodySpace(camera, body, input[0], input[1]);
                    double b = Math.toRadians(body), c = Math.toRadians(camera);
                    double worldX = local[0] * Math.cos(b) - local[1] * Math.sin(b);
                    double worldZ = local[0] * Math.sin(b) + local[1] * Math.cos(b);
                    assertEquals(input[0] * Math.cos(c) - input[1] * Math.sin(c), worldX, 1e-6);
                    assertEquals(input[0] * Math.sin(c) + input[1] * Math.cos(c), worldZ, 1e-6);
                    assertEquals(Math.hypot(input[0],input[1]),Math.hypot(local[0],local[1]),1e-6);
                }
            }
        }
    }
    @Test void bodyFacesForwardBackwardAndStrafeDirections() {
        assertEquals(30, CameraMath.movementYaw(30,0,1),1e-5);
        assertEquals(-150, CameraMath.movementYaw(30,0,-1),1e-5);
        assertEquals(-60, CameraMath.movementYaw(30,1,0),1e-5);
        assertEquals(120, CameraMath.movementYaw(30,-1,0),1e-5);
    }
    @Test void smoothingIsIndependentOfFrameRate() {
        for (double multiplier : new double[]{.1,.5,1}) {
            double at30 = 1, at144 = 1;
            for(int i=0;i<30;i++) at30 *= 1-CameraMath.frameResponse(multiplier,1.0/30);
            for(int i=0;i<144;i++) at144 *= 1-CameraMath.frameResponse(multiplier,1.0/144);
            assertEquals(at30,at144,1e-12);
        }
    }
    @Test void zeroTurnSpeedLocksFacingAndHigherSpeedTurnsFaster() {
        assertEquals(0,CameraMath.turnResponse(0));
        assertTrue(CameraMath.turnResponse(10)>CameraMath.turnResponse(5));
        assertTrue(CameraMath.turnResponse(10)<1);
        assertEquals(1,CameraMath.frameResponse(1,1.0/60));
    }
}
