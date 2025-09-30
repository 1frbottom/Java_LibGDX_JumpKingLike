package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class GameCharacter {
    // --- 1. 상태 (데이터) ---
    public Vector2 position; // 위치
    public Vector2 velocity; // 속도
    public final float MOVE_SPEED = 350.0f;
    // --- 2. 그래픽 (데이터) ---
    public Sprite sprite;    // 그리기용 스프라이트
    public boolean isGrounded = false; // '땅에 닿아있는가?' (점프 가능 여부)

    /**
     * 캐릭터 생성자
     * @param texture 이 캐릭터가 사용할 텍스처 (외부에서 로드해서 전달)
     * @param startX 시작 X 위치
     * @param startY 시작 Y 위치
     */
    public GameCharacter(Texture texture, float startX, float startY) {
        // 물리 상태 초기화
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0); // 처음엔 정지
        // 그래픽 상태 초기화
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(position.x, position.y);
    }

    public void jump() {
        if (isGrounded) {
            velocity.y = 800f; // Y축으로 점프 속도 설정
            isGrounded = false; // 점프했으니 땅에서 떨어짐
        }
    }

    // --- 3. 행동 (메서드) ---
    public void moveRight() {
        velocity.x = MOVE_SPEED;
    }
    public void moveLeft() {
        velocity.x = -MOVE_SPEED;
    }

    public void syncSpriteToPosition() {
        sprite.setPosition(position.x, position.y);
    }

    /**
     * 자신을 그립니다.
     */
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);

    }


}
