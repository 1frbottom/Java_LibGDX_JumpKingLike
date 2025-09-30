package io.jbnu.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;


public class GameWorld {
    public final float WORLD_GRAVITY = -9.8f * 200; // 초당 중력 값
    public final float FLOOR_LEVEL = 0;          // 바닥의 Y 좌표

    // --- 2. 월드 객체 ---
    private GameCharacter player;

    private final float OBJECT_SPAWN_TIME = 2.0f; // 2초마다 오브젝트 생성
    private float objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머

    private Array<CoinObject> coins; // 떨어지는 오브젝트들을 담을 배열
    private float coinSpeed = -100.0f;
    private int score;

    private Texture playerTexture;
    private Texture coinTexture;
    private Texture blockTexture;

    private Texture flagTexture;

    private float worldWidth; // 랜덤 위치 생성을 위해 월드 너비 저장
    private float worldHeight; // 랜덤 위치 생성을 위해 월드 너비 저장


    private Array<BlockObject> blocks;







    public GameWorld(int StageLevel, Texture playerTexture, Texture coinTexture, Texture blockTexture, Texture flagTexture, float worldWidth, float worldHeight)
    {
        this.playerTexture = playerTexture;
        this.coinTexture = coinTexture;
        this.blockTexture = blockTexture;
        this.flagTexture = flagTexture;
        this.worldWidth = worldWidth;

        player = new GameCharacter(playerTexture, 0, BlockObject.getWidth() + 10);
        coins = new Array<>();
        score = 0;

        for(int i = StageLevel - 1; i > 0; i--)
            coinSpeed *= 2;


        // BlockObject
        blocks = new Array<>();
        loadGround(5);
    }


    public void update(float delta) {
        // --- 1. 힘 적용 (중력, 저항) ---
        player.velocity.y += WORLD_GRAVITY * delta;

        // --- 2. '예상' 위치 계산 ---
        float oldX = player.position.x;
        float oldY = player.position.y;
        float newX = oldX + player.velocity.x * delta;
        float newY = oldY + player.velocity.y * delta;

        for (Iterator<CoinObject> iter = coins.iterator(); iter.hasNext(); )
        {
            CoinObject obj = iter.next();
            obj.update(delta);

            // 화면 밖으로 나간 오브젝트는 제거
            if (obj.position.y < FLOOR_LEVEL - obj.sprite.getHeight())
                iter.remove();

        }

        // --- 3 & 4. 충돌 검사 및 반응 ---
        player.isGrounded = false;

            // BlockObject 충돌
        Rectangle playerRect = player.sprite.getBoundingRectangle();

                // 좌우
        playerRect.setPosition(newX, oldY);
        for (BlockObject block : blocks)
            if (playerRect.overlaps(block.bounds))
            {
                newX = oldX;
                player.velocity.x = 0;

                break;
            }

                // 상하
        playerRect.setPosition(newX, newY);
        for (BlockObject block : blocks)
            if (playerRect.overlaps(block.bounds))
            {
                if (player.velocity.y < 0)
                {
                    newY = block.bounds.y + block.bounds.height;
                    player.isGrounded = true;
                }
                else if (player.velocity.y > 0)
                    newY = block.bounds.y - playerRect.height;

                player.velocity.y = 0;
                break;
            }

            // 좌우 경계 충돌
        if (newX < 0)
            newX = 0;

        if (newX > worldWidth - player.sprite.getWidth())
            newX = worldWidth - player.sprite.getWidth();

            // 바닥(FLOOR_LEVEL) 경계 충돌
        if (newY <= FLOOR_LEVEL)
        {
            newY = FLOOR_LEVEL;       // 바닥에 강제 고정
            player.velocity.y = 0;    // Y축 속도 리셋
            player.isGrounded = true; // '땅에 닿음' 상태로 변경
        }


        updateSpawning(delta);
        checkCollisions();

        // --- 5. 최종 위치 확정 ---
        // 모든 충돌 계산이 끝난 '최종' 위치를 반영
        player.position.set(newX, newY);

        // --- 6. 그래픽 동기화 ---
        player.syncSpriteToPosition();


    }


    private void updateSpawning(float delta) {

        // CoinObject
        objectSpawnTimer -= delta;
        if (objectSpawnTimer <= 0) {
            objectSpawnTimer = OBJECT_SPAWN_TIME; // 타이머 리셋

            // 월드 너비 안에서 랜덤한 X 위치 선정
            float randomX = MathUtils.random(0, worldWidth - CoinObject.Width);
            float startY = 600; // 월드 높이 (예시)
            float speed = coinSpeed; // 떨어지는 속도

            CoinObject newCoin = new CoinObject(coinTexture, randomX, startY, speed);
            coins.add(newCoin);
        }



    }

    private void checkCollisions()
    {
        // CoinObject
        for (Iterator<CoinObject> iter = coins.iterator(); iter.hasNext(); )
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

    private void loadGround(int blockNum){

        float startX = 0;
        float startY = 0;

        for(int i = 0; i < blockNum; i++)
        {
            float x = startX + (i * BlockObject.getWidth());

            blocks.add(new BlockObject(blockTexture, x, startY));
        }

        blocks.add(new BlockObject(blockTexture, 400, 350));


    }









    public int getScore() {
        return score;
    }

    public Array<CoinObject> getCoins() {
        return coins;
    }

    public Array<BlockObject> getBlocks() {
        return blocks;
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
