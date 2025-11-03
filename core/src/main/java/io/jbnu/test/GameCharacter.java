package io.jbnu.test;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;



public class GameCharacter
{
    public Vector2 position;
    public Vector2 velocity;

    public Sprite sprite;

    public boolean bIsGrounded = false;



    public GameCharacter(TextureRegion region, float startX, float startY)
    {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0); // 처음엔 정지

        this.sprite = new Sprite(region);
        this.sprite.setPosition(position.x, position.y);

    }

    public void setAnimationRegion(TextureRegion region)
    {
        boolean flipX = this.sprite.isFlipX();
        boolean flipY = this.sprite.isFlipY();

        if (this.sprite.getTexture() != region.getTexture())
            this.sprite.setTexture(region.getTexture());

        this.sprite.setRegion(region);

        this.sprite.setFlip(flipX, flipY);

    }

    public void syncSpriteToPosition()
    {
        sprite.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch)
    {
        sprite.draw(batch);
    }


}
