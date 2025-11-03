package io.jbnu.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;



public class GameScreen implements Screen
{
    // CONSTANT
    private final float WORLD_WIDTH = 1280.0f;
    private final float WORLD_HEIGHT = 720.0f;

    private final float WALL_WIDTH = 60.0f;

    // ENUM
    public enum GameState {
        RUNNING,
        PAUSED,
        CLEARED,
        GAME_OVER,
        GAME_COMPLETE
    }

    // Class Ref
    private AssetManager assets;
    private GameRenderer renderer;
    private GameWorld world;
    private CameraManager cameraManager;

    // Level
    private int currLevelIdx = 0;
    private float nextLevelTimer = -1.0f; // ◀◀◀ CLEARED 상태일 때 타이머
    private final int MAX_LEVEL = 3; // ◀◀◀ 최대 레벨 (배경 개수 기준)

    // Miscellaneous
    private GameState currentState;
    private InputProcessor inputProcessor;



    public GameScreen(AssetManager assetManager)
    {
        this.assets = assetManager;

    }

    @Override
    public void show()
    {
        currentState = GameState.RUNNING;
        currLevelIdx = 0;

        world = new GameWorld(
            assets.playerIdleAnim,
            assets.playerRunJumpAnim,
            assets.playerChargeAnim,
            assets.blockTexture,
            assets.wallTexture,
            WORLD_WIDTH,
            WALL_WIDTH,
            assets.walkSounds,
            assets.jumpSound,
            assets.landSound
            );

        cameraManager = new CameraManager(WORLD_WIDTH, WORLD_HEIGHT);

        renderer = new GameRenderer(
            world,
            cameraManager,
            assets.scoreFont,
            assets.pauseTexture,
            assets.jumpGaugeTexture,
            assets.backgroundTextures.get(0),
            assets.targetAnim,
            WORLD_WIDTH, WORLD_HEIGHT,
            WALL_WIDTH
        );

        loadCurrLevel();

        inputProcessor = new InputProcessor(world, this);
        Gdx.input.setInputProcessor(inputProcessor);

        if (assets.bgm != null)
        {
            assets.bgm.setLooping(true);
            assets.bgm.setVolume(0.4f); // (볼륨 40% 예시)
            assets.bgm.play();
        }

    }

    @Override
    public void render(float delta)
    {
        input();

        logic(delta);

        renderer.render(currentState, currLevelIdx + 1, delta);

    }

    private void logic(float delta)
    {
        if (currentState == GameState.RUNNING)
        {
            world.update(delta);
            gameEndCheck();
        }
        else if (currentState == GameState.CLEARED)
        {
            // 2초 대기 후 다음 레벨
            nextLevelTimer -= delta;
            if (nextLevelTimer <= 0)
            {
                currLevelIdx++;
                loadCurrLevel();
            }
        }

    }

    private void gameEndCheck()
    {
        if (world.isbIsCleared())
        {
            renderer.startCameraEffect();

            if (currLevelIdx == MAX_LEVEL - 1)
            {
                currentState = GameState.GAME_COMPLETE;

                if (assets.gameClearSound != null)
                    assets.gameClearSound.play(0.5f);
            }
            else
            {
                currentState = GameState.CLEARED;

                nextLevelTimer = 2.0f; // 2초 대기

                if (assets.levelClearSound != null)
                    assets.levelClearSound.play(0.5f);
            }
        }
        else if (world.isGameOver())
        {
            if (currentState != GameState.GAME_OVER)
            {
                assets.fallSound.play(0.3f);

                if (assets.bgm != null)
                    assets.bgm.stop(); // ◀◀◀ BGM 정지

                renderer.startCameraEffect();
            }

            currentState = GameState.GAME_OVER;
        }
    }

    private void loadCurrLevel()
    {
        world.loadLevel(currLevelIdx);

        // 배경 교체
        Texture bg = assets.backgroundTextures.get(currLevelIdx);
        renderer.setBackground(bg);

        // GameScreen 상태 리셋
        currentState = GameState.RUNNING;
        nextLevelTimer = -1.0f;

    }

    private void input()
    {
        if (currentState == GameState.GAME_COMPLETE)
            if (assets.bgm != null && assets.bgm.isPlaying())
                assets.bgm.stop();

        if (currentState != GameState.RUNNING)
        {
            world.setMovementLeft(false);
            world.setMovementRight(false);

            return;
        }

        boolean leftPressed = Gdx.input.isKeyPressed(Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Keys.RIGHT);

        world.setMovementLeft(leftPressed);
        world.setMovementRight(rightPressed);

    }

    public void togglePause()
    {
        if (currentState == GameState.RUNNING)
        {
            currentState = GameState.PAUSED;
            if (assets.bgm != null)
                assets.bgm.pause();
        }
        else if (currentState == GameState.PAUSED)
        {
            currentState = GameState.RUNNING;
            if (assets.bgm != null)
                assets.bgm.play();
        }
    }

    public void restartGame()
    {
        currLevelIdx = 0;

        loadCurrLevel();

        if (assets.bgm != null)
            assets.bgm.play();
    }

    @Override
    public void resize(int width, int height)
    {
        renderer.resize(width, height);
    }

    @Override
    public void dispose()
    {
        renderer.dispose();

        if (assets.bgm != null)
            assets.bgm.stop();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide()
    {
        if (assets.bgm != null)
            assets.bgm.stop();
    }

    public GameState getCurrentState()
    {
        return currentState;
    }

}
