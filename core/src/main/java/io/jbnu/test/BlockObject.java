package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;



public class BlockObject
{
    public Vector2 position;

    public Sprite sprite;

    public Rectangle bounds;



    public BlockObject(Texture texture, float startX, float startY, float width, float height)
    {

        this.sprite = new Sprite(texture);
        this.sprite.setSize(width, height);

        this.position = new Vector2(startX, startY);
        this.sprite.setPosition(position.x, position.y);

        this.bounds = new Rectangle(position.x, position.y, width, height);

    }

    public void draw(SpriteBatch batch)
    {
        sprite.draw(batch);

    }


}
