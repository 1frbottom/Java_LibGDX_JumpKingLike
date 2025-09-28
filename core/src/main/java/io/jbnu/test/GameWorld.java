package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class GameWorld {
    public final float WORLD_GRAVITY = -9.8f * 200; // 초당 중력 값
    public final float FLOOR_LEVEL = 0;          // 바닥의 Y 좌표

    // --- 2. 월드 객체 ---
    private GameCharacter player;

    private final float OBJECT_SPAWN_TIME = 2.0f; // 2초마다 오브젝트 생성
    private float objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머
    private Array<CoinObject> objects; // 떨어지는 오브젝트들을 담을 배열
    private int score;

    private Texture playerTexture;
    private Texture objectTexture;
    private float worldWidth; // 랜덤 위치 생성을 위해 월드 너비 저장
    private float worldHeight; // 랜덤 위치 생성을 위해 월드 너비 저장

    private float coinSpeed = -100.0f;

    public GameWorld(int StageLevel, Texture playerTexture, Texture objectTexture,
                     float worldWidth, float worldHeight)
    {
        this.playerTexture = playerTexture;
        this.objectTexture = objectTexture;
        this.worldWidth = worldWidth;

        player = new GameCharacter(playerTexture, worldWidth / 2, FLOOR_LEVEL);
        objects = new Array<>();
        score = 0;

        for(int i = StageLevel - 1; i > 0; i--)
            coinSpeed *= 2;

    }


    public void update(float delta) {
        // --- 1. 힘 적용 (중력, 저항) ---
        player.velocity.y += WORLD_GRAVITY * delta;
        updateSpawning(delta);

        // --- 2. '예상' 위치 계산 ---
        // (이번 프레임에 이동할 거리)
        float newX = player.position.x + player.velocity.x * delta;
        float newY = player.position.y + player.velocity.y * delta;

        for (Iterator<CoinObject> iter = objects.iterator(); iter.hasNext(); ) {
            CoinObject obj = iter.next();
            obj.update(delta);
            // 화면 밖으로 나간 오브젝트는 제거
            if (obj.position.y < FLOOR_LEVEL - obj.sprite.getHeight()) {
                iter.remove();
            }
        }

        // --- 3 & 4. 충돌 검사 및 반응 ---

            // 좌우 충돌 검사
        if (newX < 0)
            newX = 0;

        if (newX > worldWidth - player.sprite.getWidth())
            newX = worldWidth - player.sprite.getWidth();

            // 스크린 바닥(FLOOR_LEVEL)과 충돌 검사
        if (newY <= FLOOR_LEVEL) {
            newY = FLOOR_LEVEL;       // 바닥에 강제 고정
            player.velocity.y = 0;    // Y축 속도 리셋
            player.isGrounded = true; // '땅에 닿음' 상태로 변경
        } else {
            player.isGrounded = false; // 공중에 떠 있음
        }

        checkCollisions();

        // --- 5. 최종 위치 확정 ---
        // 모든 충돌 계산이 끝난 '최종' 위치를 반영
        player.position.set(newX, newY);

        // --- 6. 그래픽 동기화 ---
        player.syncSpriteToPosition();
    }


    private void updateSpawning(float delta) {
        objectSpawnTimer -= delta;
        if (objectSpawnTimer <= 0) {
            objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머 리셋

            // 월드 너비 안에서 랜덤한 X 위치 선정
            float randomX = MathUtils.random(0, worldWidth - CoinObject.CoinWidth);
            float startY = 720; // 월드 높이 (예시)
            float speed = coinSpeed; // 떨어지는 속도

            CoinObject newObject = new CoinObject(objectTexture, randomX, startY, speed);
            objects.add(newObject);
        }
    }

    private void checkCollisions()
    {
        for (Iterator<CoinObject> iter = objects.iterator(); iter.hasNext(); )
        {
            CoinObject obj = iter.next();

            if (player.sprite.getBoundingRectangle().overlaps(obj.bounds))
            {
                Main.effectSound.play();
                score++;
                System.out.println("Score: " + score);
                iter.remove();
            }
        }
    }

    public int getScore() {
        return score;
    }

    public Array<CoinObject> getObjects() {
        return objects;
    }

    // GameScreen으로부터 '점프' 입력을 받음
    public void onPlayerJump() {
        player.jump();
    }

    public void onPlayerLeft() {
        player.moveLeft();
    }

    public void onPlayerRight() {
        player.moveRight();
    }

    // GameScreen이 그릴 수 있도록 객체를 제공
    public GameCharacter getPlayer() {
        return player;
    }

}
