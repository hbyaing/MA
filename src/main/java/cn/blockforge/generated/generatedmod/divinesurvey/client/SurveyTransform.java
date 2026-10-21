package cn.blockforge.generated.generatedmod.divinesurvey.client;

import org.joml.Matrix4f;

/** Takes the terrain's view matrix and offsets world coordinates exactly once. */
public final class SurveyTransform {
    public static Matrix4f worldView(Matrix4f terrainView, double cameraX, double cameraY, double cameraZ) {
        return new Matrix4f(terrainView).translate((float)-cameraX, (float)-cameraY, (float)-cameraZ);
    }
}
