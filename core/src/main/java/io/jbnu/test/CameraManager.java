package io.jbnu.test;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraManager {
    private OrthographicCamera camera;
    private Viewport viewport;



    private float shakeDuration = 0.0f;
    private float shakeTimer = 0.0f;
    private float shakeIntensity = 0.0f;
    private Vector2 originPosition = new Vector2();
    private boolean bIsShakeStarted = false;



    public CameraManager(float worldWidth, float worldHeight)
    {
        camera = new OrthographicCamera();
        viewport = new FitViewport(worldWidth, worldHeight, camera);


    }

    public void update(float delta)
    {
        camera.update();


    }

    public void resize(int width, int height)
    {
        viewport.update(width, height, true);


    }


    public void startShake(float duration, float intensity)
    {
        if(shakeDuration > 0)
            return;

        this.shakeDuration = duration;
        this.shakeTimer = 0.0f;
        this.shakeIntensity = intensity;

        originPosition.set(camera.position.x, camera.position.y);
        bIsShakeStarted = true;

    }

    public void updateShake(float delta)
    {
        if(shakeTimer < shakeDuration)
        {
            shakeTimer += delta;

            float decayFactor = shakeTimer / shakeDuration;
            float currentIntensity = shakeIntensity * (1.0f - decayFactor);

            float offsetX = MathUtils.random(-1.0f, 1.0f) * currentIntensity;
            float offsetY = MathUtils.random(-1.0f, 1.0f) * currentIntensity;

            camera.position.x = originPosition.x + offsetX;
            camera.position.y = originPosition.y + offsetY;

        }
        else if (shakeDuration > 0)
        {
            camera.position.set(originPosition, 0);
            shakeDuration = 0.0f;
            shakeTimer = 0.0f;
            bIsShakeStarted = false;

        }

        camera.update();
    }


    public OrthographicCamera getCamera()
    {
        return camera;
    }


}
