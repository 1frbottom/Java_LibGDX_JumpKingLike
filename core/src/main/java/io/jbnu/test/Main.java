package io.jbnu.test;

//import static jdk.internal.org.jline.terminal.spi.SystemStream.Input;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private Viewport viewport;

    private GameState currentState;

    private int StageLevel = 1;

    private SpriteBatch batch;

    public static Sound effectSound;

    GameWorld world;
    private Texture objectTexture; // 떨어지는 오브젝트 텍스처
    private Texture playerTexture;
    private Sprite pauseSprite;
    private BitmapFont scoreFont;

    private final float WORLD_WIDTH = 1280;
    private final float WORLD_HEIGHT = 720;

    @Override
    public void create() {
        currentState = GameState.RUNNING;

        batch = new SpriteBatch();

        effectSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));

        playerTexture = new Texture("t.png");
        objectTexture = new Texture("coin.jpg");

        Texture pauseTexture = new Texture("pause.png");
        pauseSprite = new Sprite(pauseTexture);
        pauseSprite.setSize(200, 200);

        world = new GameWorld(StageLevel, playerTexture, objectTexture,
            this.WORLD_WIDTH, this.WORLD_HEIGHT);
        scoreFont = new BitmapFont(); // 기본 비트맵 폰트 생성
        scoreFont.getData().setScale(2);

        camera = new OrthographicCamera();
        //camera.setToOrtho(false, 800, 600);
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        input();
        logic();
        draw();
    }

    public enum GameState {
        RUNNING,
        PAUSED
    }

    private void logic(){

        if (currentState == GameState.RUNNING) {
            world.update(Gdx.graphics.getDeltaTime());

            if (world.getScore() >= 10) {
                StageLevel++;

                world = new GameWorld(StageLevel, playerTexture, objectTexture, this.WORLD_WIDTH, this.WORLD_HEIGHT);


            }
        }



    }

    private void draw(){
        camera.update();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        //

        world.getPlayer().draw(batch);
        for (CoinObject obj : world.getObjects()) {
            obj.draw(batch);
        }

        scoreFont.draw(batch, "Stage " + StageLevel, 20, WORLD_HEIGHT - 20);
        scoreFont.draw(batch, "-score: " + world.getScore(), 20, WORLD_HEIGHT - 55);

        if (currentState == GameState.PAUSED) {
            pauseSprite.setPosition(WORLD_WIDTH / 2 - pauseSprite.getWidth() / 2, WORLD_HEIGHT / 2 - pauseSprite.getHeight() / 2);
            pauseSprite.draw(batch);
        }



        //
        batch.end();
    }

    private void input() {

        // Pause
        if (Gdx.input.isKeyJustPressed(Keys.P))
            if (currentState == GameState.RUNNING)
                currentState = GameState.PAUSED;
            else if (currentState == GameState.PAUSED)
                currentState = GameState.RUNNING;

        if (currentState != GameState.RUNNING)
            return;


        // Play
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            world.onPlayerRight();
        else if (Gdx.input.isKeyPressed(Keys.LEFT))
            world.onPlayerLeft();

        if (Gdx.input.isKeyPressed(Keys.SPACE))
            world.onPlayerJump();


    }

    @Override
    public void dispose() {
        playerTexture.dispose();
        objectTexture.dispose();
        effectSound.dispose();
        scoreFont.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);


    }



}
