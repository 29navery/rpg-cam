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
    @Test void cursorFacingConvergesOnTargetDespiteShoulderOffset() {
        // Camera at (+0.5, +0.2, -4), looking forward; the target is at (+0.5, +0.2, +3).
        // The character at the origin must compensate for parallax, rather than blindly copying yaw=0.
        double x = .5, y = .2, z = 3;
        float[] look = CameraMath.lookAt(x, y, z);
        double yaw = Math.toRadians(look[0]), pitch = Math.toRadians(look[1]);
        double length = Math.sqrt(x*x + y*y + z*z);
        assertEquals(x/length,-Math.sin(yaw)*Math.cos(pitch),1e-6);
        assertEquals(y/length,-Math.sin(pitch),1e-6);
        assertEquals(z/length,Math.cos(yaw)*Math.cos(pitch),1e-6);
        assertTrue(look[0]<0);
        assertTrue(look[1]<0);
    }
    @Test void lookingStraightUpOrDownKeepsValidPitch() {
        assertEquals(-90,CameraMath.lookAt(0,10,0)[1]);
        assertEquals(90,CameraMath.lookAt(0,-10,0)[1]);
        assertEquals(-90,CameraMath.lookAt(1,0,0)[0]);
        assertEquals(90,CameraMath.lookAt(-1,0,0)[0]);
    }
    @Test void directionalHeadFollowsVerticalVelocityAndStaysLevelOnGround() {
        assertTrue(CameraMath.directionalHeadPitch(.42, false) < 0); // rising: look up
        assertTrue(CameraMath.directionalHeadPitch(-.42, false) > 0); // falling: look down
        assertEquals(0, CameraMath.directionalHeadPitch(0, false));
        assertEquals(0, CameraMath.directionalHeadPitch(.005, false));
        assertEquals(0, CameraMath.directionalHeadPitch(-.0784, true)); // grounded gravity
        assertEquals(-45, CameraMath.directionalHeadPitch(10, false));
        assertEquals(45, CameraMath.directionalHeadPitch(-10, false));
        assertEquals(0, CameraMath.directionalHeadPitch(Double.NaN, false));
    }
    @Test void distanceGlidesBothWaysButNeverClipsThroughAnObstruction() {
        float outward = CameraMath.glideDistance(2, 15, 15, 1.0 / 60);
        assertTrue(outward > 2 && outward < 15);
        float inward = CameraMath.glideDistance(15, 2, 15, 1.0 / 60);
        assertTrue(inward > 2 && inward < 15);
        assertEquals(1, CameraMath.glideDistance(15, 15, 1, 1.0 / 60));
        assertEquals(0, CameraMath.glideDistance(4, 4, 0, 1.0 / 60));
        assertEquals(4, CameraMath.glideDistance(4, 4, 15, 1.0 / 60));
        float at30 = 2, at144 = 2;
        for (int i = 0; i < 30; i++) at30 = CameraMath.glideDistance(at30, 15, 15, 1.0 / 30);
        for (int i = 0; i < 144; i++) at144 = CameraMath.glideDistance(at144, 15, 15, 1.0 / 144);
        assertEquals(at30, at144, 1e-5);
    }
    @Test void cameraDistanceDoesNotGrantAdditionalInteractionReach() {
        assertTrue(CameraMath.withinReach(4*4,0,4.5));
        assertFalse(CameraMath.withinReach(5*5,0,4.5));
        assertFalse(CameraMath.withinReach(1*1,2,4.5));
        assertTrue(CameraMath.withinReach(2*2,2,4.5));
    }

    @Test void allRoamingDirectionsCanQualifyForSprintButIdleCannot() {
        for (float[] input : new float[][]{{0,1},{0,-1},{1,0},{-1,0},{1,1},{-1,-1}}) {
            assertTrue(CameraMath.hasMovement(input[0],input[1]));
        }
        assertFalse(CameraMath.hasMovement(0,0));
    }
    @Test void interactionTurnUsesSpeedAndShortestPathWithoutSnapping() {
        float slow = CameraMath.turnTowards(0,90,3.5);
        float fast = CameraMath.turnTowards(0,90,10);
        assertTrue(slow>0 && slow<fast && fast<90);
        assertEquals(42,CameraMath.turnTowards(42,150,0));
        float acrossBoundary = CameraMath.turnTowards(179,-179,3.5);
        assertTrue(acrossBoundary>179 && acrossBoundary<181);
        float facing = 0;
        for(int i=0;i<120;i++) facing=CameraMath.turnTowards(facing,90,3.5);
        assertEquals(90,facing,1e-4);
    }

}
