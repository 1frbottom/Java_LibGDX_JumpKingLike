package io.jbnu.test;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public class BlockObject {

    public static int Width = 100;
    public static int Height = 100;

    public Vector2 position;

    public Sprite sprite;

    public Rectangle bounds;

    public BlockObject(Texture texture, float startX, float startY) {

        this.sprite = new Sprite(texture);

        this.sprite.setSize(Width, Height);

        this.position = new Vector2(startX, startY);
        this.sprite.setPosition(position.x, position.y);

        this.bounds = new Rectangle(position.x, position.y, sprite.getWidth(), sprite.getHeight());


    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);

    }

    static public int getWidth()
    {
        return Width;
    }




}
