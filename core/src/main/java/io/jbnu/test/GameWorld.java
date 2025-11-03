package io.jbnu.test;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;



public class GameWorld
{
    // CONSTANT
    private final float MAX_CHARGE_TIME = 1.0f;
    private final float MIN_JUMP_POWER = 400.0f;
    private final float MAX_JUMP_POWER = 1300.0f;
    private final float JUMP_HORIZONTAL_SPEED = 350.0f;
    private final float PLAYER_MOVE_SPEED = 300.0f;
    private final float WALK_SOUND_INTERVAL = 0.35f;
    private final int STAIRS_TO_CLEAR = 2;
    private final float WORLD_WIDTH;
    private final float WALL_WIDTH;
    private final float BASE_GRAVITY = -1960.0f;

    // World Config
    public float WORLD_GRAVITY = -1960.0f;
    private float STAIR_MIN_X_GAP = 300.0f;
    private float STAIR_MAX_X_GAP = 600.0f;
    private float STAIR_MIN_Y_GAP = 100.0f;
    private float STAIR_MAX_Y_GAP = 300.0f;
    private float currBlockWidth;
    private final float BLOCK_HEIGHT = 20.0f;

    // Game Object
    private GameCharacter player;
    private Array<BlockObject> blocks;
    private BlockObject lastBlock;
    private BlockObject firstBlock;
    public Rectangle targetBounds;

    // Asset
    private Animation<TextureRegion> playerIdleAnim;
    private Animation<TextureRegion> playerRunJumpAnim;
    private Animation<TextureRegion> playerChargeAnim;
    private Texture blockTexture;
    private Texture wallTexture;
    private Array<Sound> walkSounds;
    private Sound jumpSound;
    private Sound landSound;

    // Game State
    private boolean bIsCleared = false;
    private boolean isGameOver = false;

    // Player State
    private boolean isCharging = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private int jumpDirection = 1;
    private boolean wasWalkingLastFrame = false;
    private boolean wasGroundedLastFrame = true;

    // Timer, Counter
    private float chargeTimer = 0.0f;
    private float stateTime = 0.0f;         // for animation
    private float walkSoundTimer = 0.0f;
    private int blockCount = 0;



    public GameWorld(
        Animation<TextureRegion> idleAnim,
        Animation<TextureRegion> runJumpAnim,
        Animation<TextureRegion> chargeAnim,
        Texture blockTexture,
        Texture wallTexture,
        float WORLD_WIDTH,
        float WALL_WIDTH,
        Array<Sound> walkSounds,
        Sound jumpSound,
        Sound landSound
    )
    {
        this.playerIdleAnim = idleAnim;
        this.playerRunJumpAnim = runJumpAnim;
        this.playerChargeAnim = chargeAnim;

        this.blockTexture = blockTexture;
        this.wallTexture = wallTexture;

        this.WORLD_WIDTH = WORLD_WIDTH;
        this.WALL_WIDTH = WALL_WIDTH;

        this.walkSounds = walkSounds;
        this.jumpSound = jumpSound;
        this.landSound = landSound;

        player = new GameCharacter(idleAnim.getKeyFrame(0), 0.0f, 0.0f);
        blocks = new Array<>();

        targetBounds = new Rectangle(-100.0f, -100.0f, 0.0f, 0.0f);

        loadLevel(0);

    }

    public void loadLevel(int levelIdx)
    {
        switch (levelIdx)
        {
            case 0: // (레벨 1 - 보통)
                currBlockWidth = 300.0f;
                STAIR_MIN_X_GAP = 300.0f;
                STAIR_MAX_X_GAP = 600.0f;
                STAIR_MIN_Y_GAP = 100.0f;
                STAIR_MAX_Y_GAP = 300.0f;
                setGravity(BASE_GRAVITY);
                break;
            case 1: // (레벨 2 - 저중력)
                currBlockWidth = 200.0f;
                STAIR_MIN_X_GAP = 400.0f;
                STAIR_MAX_X_GAP = 700.0f;
                STAIR_MIN_Y_GAP = 200.0f;
                STAIR_MAX_Y_GAP = 400.0f;
                setGravity(BASE_GRAVITY * 0.6f);
                break;
            case 2: // (레벨 3 - 고중력)
                currBlockWidth = 100.0f;
                STAIR_MIN_X_GAP = 100.0f;
                STAIR_MAX_X_GAP = 300.0f;
                STAIR_MIN_Y_GAP = 50.0f;
                STAIR_MAX_Y_GAP = 100.0f;
                setGravity(BASE_GRAVITY * 1.5f);
                break;
            default:
                currBlockWidth = 300;
                setGravity(BASE_GRAVITY);
                break;
        }
        blocks.clear();

        bIsCleared = false;
        isGameOver = false;

        // 1. 바닥(시작) 블록 생성
        float startX = 100.0f;
        float startY = 100.0f;
        firstBlock = new BlockObject(blockTexture, startX, startY, currBlockWidth, BLOCK_HEIGHT);
        blocks.add(firstBlock);
        lastBlock = firstBlock;

        blockCount = 1;
        targetBounds.set(-100.0f, -100.0f, 0.0f, 0.0f);

        if (blockCount == STAIRS_TO_CLEAR)
            placeTargetFlag(firstBlock);

        // 2. 플레이어 위치 설정
        player.position.set(firstBlock.position.x + firstBlock.bounds.width / 2 - player.sprite.getWidth() / 2,
            firstBlock.position.y + firstBlock.bounds.height - 1.0f);
        player.velocity.set(0.0f, 0.0f);
        player.bIsGrounded = true;

        // 3. 점프 상태 리셋
        isCharging = false;
        chargeTimer = 0.0f;

        isMovingLeft = false;
        isMovingRight = false;

        // 걷기 관련 사운드 변수
        walkSoundTimer = 0.0f;
        wasWalkingLastFrame = false;
        wasGroundedLastFrame = true;

        for (int i = 1; i < STAIRS_TO_CLEAR; i++)
            generateNextStair();


    }

    private void generateNextStair()
    {
        float newX;
        float randGap = MathUtils.random(STAIR_MIN_X_GAP, STAIR_MAX_X_GAP);

        if (lastBlock.position.x > WORLD_WIDTH / 2)
            newX = lastBlock.position.x - randGap;
        else
            newX = lastBlock.position.x + randGap;

        newX = MathUtils.clamp(newX, 0, WORLD_WIDTH - currBlockWidth);

        float randYGap = MathUtils.random(STAIR_MIN_Y_GAP, STAIR_MAX_Y_GAP);
        float newY = lastBlock.position.y + randYGap;

        BlockObject newBlock = new BlockObject(blockTexture, newX, newY, currBlockWidth, BLOCK_HEIGHT);
        blocks.add(newBlock);
        lastBlock = newBlock;

        blockCount++;
        if (blockCount == STAIRS_TO_CLEAR)
            placeTargetFlag(newBlock);

    }

    private void placeTargetFlag(BlockObject targetBlock)
    {
        float flagWidth = 80.0f;
        float flagHeight = 80.0f;

        // 깃발 위치 (블록 중앙 상단)
        float flagX = targetBlock.position.x + (targetBlock.bounds.width / 2) - (flagWidth / 2);
        float flagY = targetBlock.position.y + targetBlock.bounds.height;

        // 깃발의 충돌 영역(Bounds)을 실제 월드 위치에 생성
        targetBounds.set(flagX, flagY, flagWidth, flagHeight);

    }

    private void manageBlocks()
    {
        Iterator<BlockObject> iter = blocks.iterator();
        while (iter.hasNext())
        {
            BlockObject block = iter.next();
            if ((block != firstBlock) && (player.position.y - block.position.y) > (720 * 1.5f))
                iter.remove();
        }

    }

    public void update(float delta)
    {
        if (isCharging)
            chargeTimer += delta;

        // --- 2. 중력 적용
        player.velocity.y += WORLD_GRAVITY * delta;

        // --- 3. 예상 위치 계산
        float oldX = player.position.x;
        float oldY = player.position.y;

        float newY = oldY + player.velocity.y * delta;

        // --- 4. 충돌 검사
        Rectangle playerRect = player.sprite.getBoundingRectangle();

        // --- 수직 충돌
        player.bIsGrounded = false; // Y축 충돌 검사 직전에 리셋
        playerRect.setPosition(oldX, newY); // [중요] Y축만 검사하므로 oldX 사용

        for (BlockObject block : blocks)
            if (playerRect.overlaps(block.bounds))
            {
                if (player.velocity.y <= 0)
                {
                    newY = block.bounds.y + block.bounds.height;
                    player.bIsGrounded = true;
                }
                else if (player.velocity.y > 0)
                    newY = block.bounds.y - playerRect.height;

                player.velocity.y = 0; // 수직 속도 0
                break;
            }

        // --- X축 속도 결정
        if (isCharging)
            player.velocity.x = 0;
        else if (player.bIsGrounded)
            if (isMovingLeft && !isMovingRight)
            {
                // 2. 왼쪽 이동
                player.velocity.x = -PLAYER_MOVE_SPEED;
                player.sprite.setFlip(true, false);
            }
            else if (isMovingRight && !isMovingLeft)
            {
                // 3. 오른쪽 이동
                player.velocity.x = PLAYER_MOVE_SPEED;
                player.sprite.setFlip(false, false);
            }
            else
            {
                // 4. 땅에서 멈춤 (미끄러짐 방지)
                player.velocity.x = 0;
            }

        // --- X축 예상 위치 계산
        float newX = oldX + player.velocity.x * delta;

        // --- 수평 충돌
        playerRect.setPosition(newX, newY);
        for (BlockObject block : blocks)
            if (playerRect.overlaps(block.bounds))
            {
                newX = oldX; // 수평 이동 취소
                player.velocity.x = 0;

                break;
            }

        // 4-3. 좌우 경계 충돌
        if (newX < WALL_WIDTH)
        {
            newX = WALL_WIDTH; // 0에서 수정
            player.velocity.x = 0;
        }

        float rightBoundary = WORLD_WIDTH - WALL_WIDTH - player.sprite.getWidth();
        if (newX > rightBoundary)
        {
            newX = rightBoundary;
            player.velocity.x = 0;
        }

        // --- 5. 타겟 충돌 검사
        playerRect.setPosition(newX, newY);

        if (playerRect.overlaps(targetBounds))
            bIsCleared = true;

        if (bIsCleared)
        {
            player.velocity.set(0, 0);
            return;
        }

        // --- 6. 낙사
        if (newY < firstBlock.position.y - 720)
        {
            isGameOver = true;
            return;
        }

        stateTime += delta;

        updatePlayerSprite(delta);

        // --- 7. 최종 위치 확정 및 그래픽 동기화
        player.position.set(newX, newY);
        player.syncSpriteToPosition();

        if (player.bIsGrounded && !wasGroundedLastFrame)
            if (landSound != null)
                landSound.play(0.15f);

        // (현재 프레임의 상태를 다음 프레임을 위해 저장)
        wasGroundedLastFrame = player.bIsGrounded;

        // --- 8. 블록 생성/삭제
        manageBlocks();

    }

    public void startJumpCharge()
    {
        if (player.bIsGrounded)
        {
            isCharging = true;
            chargeTimer = 0f;

            if (player.sprite.isFlipX())
                jumpDirection = -1;
            else
                jumpDirection = 1;

        }

    }

    public void releaseJump()
    {
        if (isCharging)
        {
            jumpSound.play(0.1f);

            isCharging = false;
            float powerRatio = Math.min(chargeTimer / MAX_CHARGE_TIME, 1.0f);
            float jumpPower = MIN_JUMP_POWER + (MAX_JUMP_POWER - MIN_JUMP_POWER) * powerRatio;

            player.velocity.y = jumpPower;

            // 수평 속도 설정
            player.velocity.x = jumpDirection * JUMP_HORIZONTAL_SPEED;

            player.bIsGrounded = false;
        }

    }

    public void setMovementLeft(boolean active)
    {
        this.isMovingLeft = active;

    }

    public void setMovementRight(boolean active)
    {
        this.isMovingRight = active;

    }

    public GameCharacter getPlayer()
    {
        return player;
    }

    public Array<BlockObject> getBlocks()
    {
        return blocks;
    }

    public boolean isbIsCleared()
    {
        return bIsCleared;
    }
    public boolean isGameOver()
    {
        return isGameOver;
    }

    public boolean isCharging()
    {
        return isCharging;
    }

    public float getChargeRatio()
    {
        if (!isCharging)
            return 0.0f;

        return Math.min(chargeTimer / MAX_CHARGE_TIME, 1.0f);
    }

    private void updatePlayerSprite(float delta)
    {
        TextureRegion currentFrame = null;
        boolean nowWalking = false;

        // 우선순위 1: 공중
        if (!player.bIsGrounded)
        {
            currentFrame = playerRunJumpAnim.getKeyFrame(stateTime, true);
            player.sprite.setSize(80, 80);
        }
        else if (isCharging)
        {
            currentFrame = playerChargeAnim.getKeyFrame(stateTime, true);
            player.sprite.setSize(70, 70);
        }
        else if (player.velocity.x != 0)
        {
            nowWalking = true;
            currentFrame = playerRunJumpAnim.getKeyFrame(stateTime, true);
            player.sprite.setSize(80, 80);
        }
        else
        {
            currentFrame = playerIdleAnim.getKeyFrame(stateTime, true);
            player.sprite.setSize(70, 70);
        }

        player.setAnimationRegion(currentFrame);

        if (nowWalking)
        {
            walkSoundTimer += delta; // 걷는 중이면 타이머 증가

            // 1. 걷기 시작한 첫 프레임인가?
            if (!wasWalkingLastFrame)
            {
                playRandomWalkSound(); // 즉시 첫 걸음 소리 재생
                walkSoundTimer = 0f; // 타이머 리셋
            }
            // 2. 걷는 중이고, 일정 간격이 지났나?
            else if (walkSoundTimer > WALK_SOUND_INTERVAL)
            {
                playRandomWalkSound(); // 다음 걸음 소리 재생
                walkSoundTimer = 0f; // 타이머 리셋
            }
        } else
            walkSoundTimer = 0f;

        wasWalkingLastFrame = nowWalking;
    }

    private void playRandomWalkSound()
    {
        if (walkSounds != null && walkSounds.size > 0)
        {
            int index = MathUtils.random(walkSounds.size - 1);
            walkSounds.get(index).play(0.25f);
        }

    }

    public Texture getWallTexture()
    {
        return this.wallTexture;
    }

    public void setGravity(float newGravity)
    {
        this.WORLD_GRAVITY = newGravity;
    }

    public Rectangle getTargetBounds()
    {
        return targetBounds;
    }


}
