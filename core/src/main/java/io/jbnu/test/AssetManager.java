package io.jbnu.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;



public class AssetManager {
    // Atlas
    private TextureAtlas playerRunJumpAtlas;
    public Animation<TextureRegion> playerRunJumpAnim;

    private TextureAtlas playerIdleAtlas;
    public Animation<TextureRegion> playerIdleAnim;

    private TextureAtlas playerChargeAtlas;
    public Animation<TextureRegion> playerChargeAnim;

    private TextureAtlas targetAtlas;
    public Animation<TextureRegion> targetAnim;

    // Texture
    public Texture blockTexture;
    public Texture wallTexture;
    public Texture pauseTexture;
    public Texture jumpGaugeTexture;
    public Array<Texture> backgroundTextures;

    public BitmapFont scoreFont;

    // Sound
    public Music bgm;
    public Sound levelClearSound;
    public Sound gameClearSound;

    public Array<Sound> walkSounds;
    public Sound jumpSound;
    public Sound landSound;
    public Sound fallSound;



    public void load()
    {
        // Atlas
        playerIdleAtlas = new TextureAtlas(Gdx.files.internal("gifs/idle.atlas"));
        Array<TextureAtlas.AtlasRegion> idleRegions = playerIdleAtlas.findRegions("frame");
        playerIdleAnim = new Animation<>(0.1f, idleRegions, Animation.PlayMode.LOOP);

        playerRunJumpAtlas = new TextureAtlas(Gdx.files.internal("gifs/run.atlas"));
        Array<TextureAtlas.AtlasRegion> runRegions = playerRunJumpAtlas.findRegions("frame");
        playerRunJumpAnim = new Animation<>(0.1f, runRegions, Animation.PlayMode.LOOP);

        playerChargeAtlas = new TextureAtlas(Gdx.files.internal("gifs/charge.atlas"));
        Array<TextureAtlas.AtlasRegion> chargeRegions = playerChargeAtlas.findRegions("frame");
        playerChargeAnim = new Animation<>(0.1f, chargeRegions, Animation.PlayMode.LOOP);

        targetAtlas = new TextureAtlas(Gdx.files.internal("gifs/target.atlas"));
        Array<TextureAtlas.AtlasRegion> targetRegions = targetAtlas.findRegions("frame");
        targetAnim = new Animation<>(0.1f, targetRegions, Animation.PlayMode.LOOP);

        // Texture
        blockTexture = new Texture("block.png");
        wallTexture = new Texture("wall.png");
        pauseTexture = new Texture("pause.png");
        jumpGaugeTexture = new Texture("jumpGauge.png");

        scoreFont = new BitmapFont();
        scoreFont.getData().setScale(2);

        // Background
        backgroundTextures = new Array<>();
        Texture bg1 = new Texture("background/background_1.png");
        Texture bg2 = new Texture("background/background_2.png");
        Texture bg3 = new Texture("background/background_3.png");
        bg1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        bg3.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        backgroundTextures.add(bg1);
        backgroundTextures.add(bg2);
        backgroundTextures.add(bg3);

        // Sound
        bgm = Gdx.audio.newMusic(Gdx.files.internal("sound/bgm_medieval.mp3"));
        levelClearSound = Gdx.audio.newSound(Gdx.files.internal("sound/clear_level.mp3"));
        gameClearSound = Gdx.audio.newSound(Gdx.files.internal("sound/clear_game.mp3"));

        walkSounds = new Array<>();

        Sound walkSound_1 = Gdx.audio.newSound(Gdx.files.internal("sound/walk_1.mp3"));
        Sound walkSound_2 = Gdx.audio.newSound(Gdx.files.internal("sound/walk_2.mp3"));
        Sound walkSound_3 = Gdx.audio.newSound(Gdx.files.internal("sound/walk_3.mp3"));

        walkSounds.add(walkSound_1);
        walkSounds.add(walkSound_2);
        walkSounds.add(walkSound_3);

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("sound/jump.mp3"));
        landSound = Gdx.audio.newSound(Gdx.files.internal("sound/land.mp3"));
        fallSound = Gdx.audio.newSound(Gdx.files.internal("sound/fall.mp3"));

    }

    public void dispose()
    {
        // Atlas
        playerIdleAtlas.dispose();
        playerRunJumpAtlas.dispose();
        playerChargeAtlas.dispose();
        targetAtlas.dispose();

        // Texture
        blockTexture.dispose();
        wallTexture.dispose();
        pauseTexture.dispose();
        scoreFont.dispose();
        jumpGaugeTexture.dispose();

        for (Texture tex : backgroundTextures)
            tex.dispose();

        // Sound
        for (Sound sound : walkSounds)
            sound.dispose();

        jumpSound.dispose();
        fallSound.dispose();
        levelClearSound.dispose();
        gameClearSound.dispose();
        landSound.dispose();
        bgm.dispose();


    }


}
