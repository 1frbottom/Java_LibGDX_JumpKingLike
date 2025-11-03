package io.jbnu.test;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;



public class GameRenderer
{
    // CONSTANT
    private final float WORLD_WIDTH;
    private final float WORLD_HEIGHT;

    private final float WALL_WIDTH;

    private final float PARALLAX_SPEED_X = 0.02f;
    private final float PARALLAX_SPEED_Y = 0.05f;

    // Core
    private GameWorld world;
    private SpriteBatch batch;

    // Camera
    private CameraManager cameraManager;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;

    // UI
    private BitmapFont scoreFont;
    private Sprite pauseSprite;
    private Texture currentBackground;
    private Texture jumpGaugeTexture;

    // Target
    private Animation<TextureRegion> targetAnim;
    private float rendererStateTime = 0.0f;



    public GameRenderer
    (
        GameWorld world,
        CameraManager cameraManager,
        BitmapFont font,
        Texture pauseTexture,
        Texture jumpGaugeTexture,
        Texture initialBackground,
        Animation<TextureRegion> targetAnim,
        float WORLD_WIDTH, float WORLD_HEIGHT,
        float WALL_WIDTH
    )
    {
        // CONSTANT
        this.WORLD_WIDTH = WORLD_WIDTH;
        this.WORLD_HEIGHT = WORLD_HEIGHT;

        this.WALL_WIDTH = WALL_WIDTH;

        // Core
        this.world = world;
        this.batch = new SpriteBatch();

        // Camera
        this.cameraManager = cameraManager;
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(this.WORLD_WIDTH, this.WORLD_HEIGHT, uiCamera);

        // Ui
        this.scoreFont = font;
        this.currentBackground = initialBackground;
        this.jumpGaugeTexture = jumpGaugeTexture;
        this.pauseSprite = new Sprite(pauseTexture);
        this.pauseSprite.setSize(200, 200);

        // Target
        this.targetAnim = targetAnim;

    }

    public void render(GameScreen.GameState currentState, int StageLevel, float delta)
    {
        if (currentState == GameScreen.GameState.RUNNING)
            rendererStateTime += delta;

        Vector2 playerPos = world.getPlayer().position;

        // 1. 게임 카메라 로직
        if (currentState == GameScreen.GameState.RUNNING || currentState == GameScreen.GameState.PAUSED)
            cameraManager.update(playerPos);

        // (CLEARED 또는 GAME_OVER 시 카메라 쉐이크)
        // GameScreen이 gameEndCheck()에서 startCameraEffect()를 호출함
        cameraManager.updateShake(delta);

        ScreenUtils.clear(1f, 1f, 1f, 1f);

        // === 3. 게임 월드 그리기 (Game Camera 사용) ===
        batch.setProjectionMatrix(cameraManager.getCamera().combined);
        batch.begin();

        drawParallaxBackground();

        Texture wallTexture = world.getWallTexture();

        // 1. 원본 텍스처의 픽셀 비율 (높이 / 너비)을 계산합니다.
        float texPixelWidth = wallTexture.getWidth();
        float texPixelHeight = wallTexture.getHeight();
        float aspectRatio = texPixelHeight / texPixelWidth;

        // 2. 타일 1개의 월드 유닛 높이를 계산합니다. (너비는 WALL_WIDTH인 20f로 고정)
        float tileHeight = WALL_WIDTH * aspectRatio;

        // 3. 시작 Y좌표(-10000)부터 끝 Y좌표(+10000)까지 타일을 반복해서 그립니다.
        float wallStartY = -10000f;
        float wallTotalHeight = 20000f;
        float wallEndY = wallStartY + wallTotalHeight;

        for (float currentY = wallStartY; currentY < wallEndY; currentY += tileHeight)
        {
            batch.draw(
                wallTexture,
                0,
                currentY,
                WALL_WIDTH,
                tileHeight
            );

            // 오른쪽 벽
            batch.draw(
                wallTexture,
                WORLD_WIDTH - WALL_WIDTH,
                currentY,
                WALL_WIDTH,
                tileHeight
            );

        }

        world.getPlayer().draw(batch);

        for (BlockObject b : world.getBlocks())
            b.draw(batch);

        // 2. 깃발(타겟)을 GameWorld의 targetBounds 위치에 그립니다.
        Rectangle target = world.getTargetBounds();

        // (targetBounds의 x좌표가 0 이상이면 유효한 깃발로 간주)
        if (target.x >= 0)
        {
            TextureRegion targetFrame = targetAnim.getKeyFrame(rendererStateTime, true);

            // GameWorld에서 설정한 Bounds 위치/크기 그대로 그리기
            batch.draw(
                targetFrame,
                target.x,
                target.y,
                target.width,
                target.height
            );
        }

        batch.end();

        // === 4. UI
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        // 4-1. 스테이지 레벨
        scoreFont.draw(batch, "Stage " + StageLevel, 20, WORLD_HEIGHT - 20);

        if (world.isCharging())
        {
            float chargeRatio = world.getChargeRatio();

            // 게이지 바 크기 및 위치 설정
            float GAUGE_MAX_WIDTH = 250f;
            float GAUGE_HEIGHT = 25f;

            float gaugeX = (WORLD_WIDTH / 2) - (GAUGE_MAX_WIDTH / 2);
            float gaugeY = 100f; // 화면 하단에서 100px 위

            // 1. 게이지 배경 (어두운 회색)
            batch.setColor(0.3f, 0.3f, 0.3f, 0.8f);
            batch.draw(jumpGaugeTexture, gaugeX, gaugeY, GAUGE_MAX_WIDTH, GAUGE_HEIGHT);

            // 2. 게이지 채우기 (충전량만큼 노란색)
            batch.setColor(1f, 1f, 0.2f, 1f);
            batch.draw(jumpGaugeTexture, gaugeX, gaugeY, GAUGE_MAX_WIDTH * chargeRatio, GAUGE_HEIGHT);

            // 3. (중요) 이후 다른 스프라이트에 영향이 없도록 색상 초기화
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // 4-2. 일시정지
        if (currentState == GameScreen.GameState.PAUSED)
        {
            pauseSprite.setPosition(WORLD_WIDTH / 2 - pauseSprite.getWidth() / 2, WORLD_HEIGHT / 2 - pauseSprite.getHeight() / 2);
            pauseSprite.draw(batch);

        }

        // 4-4. (유지) 게임 오버 / 클리어 텍스트
        if (currentState == GameScreen.GameState.GAME_OVER)
        {
            scoreFont.draw(batch, "GAME OVER (Press R)", WORLD_WIDTH / 2 - 150, WORLD_HEIGHT / 2);
        }
        else if (currentState == GameScreen.GameState.CLEARED)
        {
            scoreFont.draw(batch, "LEVEL CLEARED!", WORLD_WIDTH / 2 - 120, WORLD_HEIGHT / 2);
        }
        else if (currentState == GameScreen.GameState.GAME_COMPLETE)
        {
            scoreFont.draw(batch, "GAME CLEAR! (Press R)", WORLD_WIDTH / 2 - 160, WORLD_HEIGHT / 2);
        }

        batch.end();
    }

    private void drawParallaxBackground()
    {
        OrthographicCamera gameCamera = cameraManager.getCamera();

        float camLeft = gameCamera.position.x - gameCamera.viewportWidth / 2;
        float camBottom = gameCamera.position.y - gameCamera.viewportHeight / 2;

        float camWidth = gameCamera.viewportWidth;
        float camHeight = gameCamera.viewportHeight;

        // (X축은 고정, Y축만 스크롤)
        // 1. 스크롤 시작 위치(v)를 뷰포트 높이 기준으로 계산
        float u = (gameCamera.position.x * PARALLAX_SPEED_X) / gameCamera.viewportWidth; // ◀◀◀ 추가
        float v = (gameCamera.position.y * PARALLAX_SPEED_Y) / gameCamera.viewportHeight;
        // 2. 텍스처 크기와 상관없이 항상 뷰포트 높이(1.0)만큼 샘플링
        float v2 = v + 1.0f; // 1.0f 만큼만 샘플링
        float u2 = u + 1.0f; // 1.0f 만큼만 샘플링

        // 1. 카메라(뷰포트)와 텍스처의 원본 비율 (너비 / 높이) 계산
        float camRatio = camWidth / camHeight;
        float texRatio = (float)currentBackground.getWidth() / (float)currentBackground.getHeight();

        float drawWidth, drawHeight, drawX, drawY;

        if (camRatio > texRatio)
        {
            // 카메라(뷰포트)가 텍스처보다 *가로로 더 넓은* 경우 (예: 16:9 카메라, 4:3 텍스처)
            // -> 텍스처의 '너비'를 뷰포트 너비에 맞춥니다 (Cover).
            drawWidth = camWidth;
            // -> 비율에 따라 높이를 계산합니다 (뷰포트 높이보다 커짐).
            drawHeight = drawWidth / texRatio;
            // -> 뷰포트 밖으로 튀어나간 높이를 상하로 정렬합니다.
            drawX = camLeft;
            drawY = camBottom - (drawHeight - camHeight) / 2.0f;
        } else
        {
            // 카메라(뷰포트)가 텍스처보다 *세로로 더 길거나* 같은 경우 (예: 9:16 카메라, 16:9 텍스처)
            // -> 텍스처의 '높이'를 뷰포트 높이에 맞춥니다 (Cover).
            drawHeight = camHeight;
            // -> 비율에 따라 너비를 계산합니다 (뷰포트 너비보다 커짐).
            drawWidth = drawHeight * texRatio;
            // -> 뷰포트 밖으로 튀어나간 너비를 좌우로 정렬합니다.
            drawX = camLeft - (drawWidth - camWidth) / 2.0f;
            drawY = camBottom;
        }

        // 확대 조정
        final float BACKGROUND_ZOOM_FACTOR = 1.15f;

        float zoomedWidth = drawWidth * BACKGROUND_ZOOM_FACTOR;
        float zoomedHeight = drawHeight * BACKGROUND_ZOOM_FACTOR;

        float newDrawX = drawX - (zoomedWidth - drawWidth) / 2.0f;
        float newDrawY = drawY - (zoomedHeight - drawHeight) / 2.0f;

        batch.draw(
            currentBackground,
            newDrawX, newDrawY,          // (계산된 위치)
            zoomedWidth, zoomedHeight, // (비율이 유지된 크기)
            u, v2, u2, v           // (스크롤을 위한 텍스처 좌표)
        );

    }

    public void resize(int width, int height)
    {
        cameraManager.resize(width, height);
        uiViewport.update(width, height, true);

    }

    public void setBackground(Texture newBackground)
    {
        this.currentBackground = newBackground;

    }

    public void startCameraEffect()
    {
        cameraManager.startShake(0.5f, 20.0f);

    }

    public void dispose()
    {
        batch.dispose();

    }
}
