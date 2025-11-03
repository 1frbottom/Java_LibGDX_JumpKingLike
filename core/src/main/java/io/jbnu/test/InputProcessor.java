package io.jbnu.test;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;



public class InputProcessor extends InputAdapter
{

    private final GameWorld world;
    private final GameScreen gameScreen;



    public InputProcessor(GameWorld world, GameScreen gameScreen)
    {
        this.world = world;
        this.gameScreen = gameScreen;

    }

    @Override
    public boolean keyUp(int keycode)
    {
        if (gameScreen.getCurrentState() != GameScreen.GameState.RUNNING)
            return false;

        if (keycode == Keys.SPACE)
        {
            world.releaseJump();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyDown(int keycode)
    {

        if (keycode == Keys.R)
            if (
                (gameScreen.getCurrentState() == GameScreen.GameState.GAME_OVER) ||
                (gameScreen.getCurrentState() == GameScreen.GameState.GAME_COMPLETE)
            )
            {
                gameScreen.restartGame();

                return true;
            }

        if (keycode == Keys.P)
            if (
                gameScreen.getCurrentState() == GameScreen.GameState.RUNNING ||
                gameScreen.getCurrentState() == GameScreen.GameState.PAUSED)
            {
                gameScreen.togglePause();

                return true;
            }

        if (gameScreen.getCurrentState() != GameScreen.GameState.RUNNING)
            return false;

        if (keycode == Keys.SPACE)
        {
            world.startJumpCharge();

            return true;
        }

        return false;
    }


}
