package io.jbnu.test;

import com.badlogic.gdx.Game;



public class Main extends Game
{
    private AssetManager assetManager;



    @Override
    public void create()
    {
        assetManager = new AssetManager();
        assetManager.load();

        this.setScreen(new GameScreen(assetManager));

    }

    @Override
    public void render()
    {
        super.render();

    }

    @Override
    public void dispose()
    {
        assetManager.dispose();
        super.dispose();

    }

}
