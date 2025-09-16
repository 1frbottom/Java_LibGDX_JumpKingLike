package io.jbnu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */



public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Texture tralTexture;
    private Sprite tralSprite1;
    private Sprite tralSprite2;

    Sound effectSound;
    Float tPosX = 0.0f;
    Float tPosY = 0.0f;
    float speed = 1000.0f;
    int screenWidth = 1280;
    int screenHeight = 720;
    int characterWidth = 168;
    int characterHeight = 142;

    Vector2 touchPos;
    Array<Sprite> tSprites;

    boolean tempFlag = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");

        tralTexture = new Texture("t.png");

        tralSprite1 = new Sprite(tralTexture);
        tralSprite2 = new Sprite(tralTexture);



        effectSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));

        touchPos = new Vector2();
        tSprites = new Array<>();

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        Input();
        //logic();
        draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        tralTexture.dispose();


    }

    private void Input() {
        float delta = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
            tPosX = MathUtils.clamp(tPosX + (speed * delta), 0, screenWidth - characterWidth);
            tralSprite1.setX(tPosX);
        }
        if(Gdx.input.isKeyPressed(Keys.LEFT)) {
            tPosX = MathUtils.clamp(tPosX - (speed * delta), 0, screenWidth - characterWidth);
            tralSprite1.setX(tPosX);

        }
        if(Gdx.input.isKeyPressed(Keys.UP)) {
            tPosY = MathUtils.clamp(tPosY + (speed * delta), 0, screenHeight - characterHeight);
            tralSprite1.setY(tPosY);

        }
        if(Gdx.input.isKeyPressed(Keys.DOWN)) {
            tPosY = MathUtils.clamp(tPosY - (speed * delta), 0, screenHeight - characterHeight);
            tralSprite1.setY(tPosY);

        }

        // should be clamped
//        if(Gdx.input.isTouched()) {
//            touchPos.set(Gdx.input.getX(), screenHeight - Gdx.input.getY());
//            tralSprite.setCenterX(touchPos.x);
//            tralSprite.setCenterY(touchPos.y);
//
//        }

        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), screenHeight - Gdx.input.getY());

            Sprite newSpr = new Sprite(tralTexture);
            newSpr.setCenter(touchPos.x, touchPos.y);

            tSprites.add(newSpr);

        }

        if(Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            tempFlag = true;
            logic();

        }


    }

    public void draw()
    {
        batch.begin();

        if(tempFlag)
        {
            tralSprite1.draw(batch);
            tralSprite2.draw(batch);
        }

        batch.end();
    }

    public void logic()
    {

        tralSprite1.setSize(300,300);
        tralSprite2.setSize(200,200);
        tralSprite1.setPosition(MathUtils.random(0, screenWidth),  MathUtils.random(0, screenHeight));
        tralSprite2.setPosition(MathUtils.random(0, screenWidth),  MathUtils.random(0, screenHeight));

        if(isCollideRectangle1(tralSprite1, tralSprite2))
            System.out.println("Collide");

    }


    public boolean isCollideRectangle1(Sprite sp1, Sprite sp2)
    {
        if(sp1.getX() + sp1.getWidth() < sp2.getX())
            return false;
        if(sp1.getX() > sp2.getX() + sp2.getWidth())
            return false;
        if(sp1.getY() + sp1.getHeight() < sp2.getY())
            return false;
        if(sp1.getY() + sp2.getY() < sp2.getHeight())
            return false;

        return true;
    }

    public boolean isCollideRectangle2(Sprite sp1, Sprite sp2)
    {
        Rectangle r1 = new Rectangle(sp1.getX(), sp1.getY(), sp1.getWidth(), sp1.getHeight());
        Rectangle r2 = new Rectangle(sp2.getX(), sp2.getY(), sp2.getWidth(), sp2.getHeight());

        return r1.overlaps(r2);
    }

    public boolean isCollideCircle(Sprite sp1, Sprite sp2) {
        Circle c1 = new Circle(sp1.getX() + sp1.getWidth() / 2, sp1.getY() + sp1.getHeight() / 2, sp1.getWidth() / 2);
        Circle c2 = new Circle(sp2.getX() + sp2.getWidth() / 2, sp2.getY() + sp2.getHeight() / 2, sp2.getWidth() / 2);

        return c1.overlaps(c2);
    }

}

